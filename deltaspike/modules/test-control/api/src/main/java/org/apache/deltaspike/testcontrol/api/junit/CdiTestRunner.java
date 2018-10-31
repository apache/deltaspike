/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.testcontrol.api.junit;

import junit.framework.Assert;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.core.util.ServiceUtils;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.literal.TestControlLiteral;
import org.apache.deltaspike.testcontrol.spi.ExternalContainer;
import org.apache.deltaspike.testcontrol.spi.TestAware;
import org.apache.deltaspike.testcontrol.spi.TestControlValidator;
import org.apache.deltaspike.testcontrol.spi.junit.TestStatementDecoratorFactory;
import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Boolean.TRUE;

/**
 * A JUnit test runner to start up with a CDI or embedded JavaEE container.
 *
 * <p>If the underlying container supports it you can even pass in  a property file
 * to the bootstrap. You can configure the name of the configuration file via a
 * {@link org.apache.deltaspike.core.api.config.ConfigResolver} property named
 * <code>cdiTestRunnerConfig</code>. By default the name of the config file is
 * <code>cdiTestRunnerConfig.properties</code>.</p>
 */
public class CdiTestRunner extends BlockJUnit4ClassRunner
{
    private static final Logger LOGGER = Logger.getLogger(CdiTestRunner.class.getName());

    private static final boolean USE_TEST_CLASS_AS_CDI_BEAN;
    private static final boolean ALLOW_INJECTION_POINT_MANIPULATION;

    private static Set<Integer> notifierIdentities = new CopyOnWriteArraySet<Integer>();

    static
    {
        USE_TEST_CLASS_AS_CDI_BEAN = TestBaseConfig.ContainerIntegration.USE_TEST_CLASS_AS_CDI_BEAN;
        ALLOW_INJECTION_POINT_MANIPULATION = TestBaseConfig.MockIntegration.ALLOW_MANUAL_INJECTION_POINT_MANIPULATION;
    }

    private static ThreadLocal<Boolean> automaticScopeHandlingActive = new ThreadLocal<Boolean>();

    private static ThreadLocal<CdiTestRunner> currentTestRunner = new ThreadLocal<CdiTestRunner>();

    private List<TestStatementDecoratorFactory> statementDecoratorFactories;

    private ContainerAwareTestContext testContext;

    public CdiTestRunner(Class<?> testClass) throws InitializationError
    {
        super(testClass);

        TestControl testControl = testClass.getAnnotation(TestControl.class);
        this.testContext = new ContainerAwareTestContext(testControl, null);

        //benefits from the fallback-handling in ContainerAwareTestContext
        Class<? extends Handler> logHandlerClass = this.testContext.getLogHandlerClass();

        if (!Handler.class.equals(logHandlerClass))
        {
            try
            {
                LOGGER.addHandler(logHandlerClass.newInstance());
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }

        this.statementDecoratorFactories = ServiceUtils.loadServiceImplementations(TestStatementDecoratorFactory.class);
        Collections.sort(this.statementDecoratorFactories, new Comparator<TestStatementDecoratorFactory>()
        {
            @Override
            public int compare(TestStatementDecoratorFactory f1, TestStatementDecoratorFactory f2)
            {
                return f1.getOrdinal() > f2.getOrdinal() ? 1 : -1;
            }
        });
    }

    @Override
    public void run(RunNotifier runNotifier)
    {
        try
        {
            CdiTestSuiteRunner.getCdiTestRunnerExecutionRef().set(TRUE);

            if (!CdiTestSuiteRunner.isContainerStarted()) //not called as a part of a test-suite
            {
                int identityHashCode = System.identityHashCode(runNotifier);
                if (!notifierIdentities.contains(identityHashCode))
                {
                    addLogRunListener(runNotifier, identityHashCode);
                }
            }

            super.run(runNotifier);

        }
        finally
        {
            CdiTestSuiteRunner.getCdiTestRunnerExecutionRef().set(null);
            CdiTestSuiteRunner.getCdiTestRunnerExecutionRef().remove();
        }
    }

    private static synchronized void addLogRunListener(RunNotifier notifier, int identityHashCode)
    {
        if (notifierIdentities.contains(identityHashCode))
        {
            return;
        }
        notifierIdentities.add(identityHashCode);
        notifier.addListener(new CdiTestSuiteRunner.LogRunListener());
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test)
    {
        return new ContainerAwareMethodInvoker(method, test);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier)
    {
        currentTestRunner.set(this);

        TestControl testControl = method.getAnnotation(TestControl.class);

        ContainerAwareTestContext currentTestContext =
                new ContainerAwareTestContext(testControl, this.testContext);

        currentTestContext.applyBeforeMethodConfig(method.getMethod());

        try
        {
            super.runChild(method, notifier);
        }
        finally
        {
            currentTestContext.applyAfterMethodConfig();
        }
    }

    @Override
    protected Object createTest() throws Exception
    {
        BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();

        Class<?> type = getTestClass().getJavaClass();
        Set<Bean<?>> beans = beanManager.getBeans(type);

        Object result;
        if (!USE_TEST_CLASS_AS_CDI_BEAN || beans == null || beans.isEmpty())
        {
            result = super.createTest();
            BeanProvider.injectFields(result); //fallback to simple injection
        }
        else
        {
            Bean<Object> bean = (Bean<Object>) beanManager.resolve(beans);
            CreationalContext<Object> creationalContext = beanManager.createCreationalContext(bean);
            result = beanManager.getReference(bean, type, creationalContext);
        }
        return result;
    }

    //TODO use Rules instead
    @Override
    protected Statement withBefores(FrameworkMethod method, Object target, Statement statement)
    {
        Statement result = super.withBefores(method, target, statement);
        result = wrapBeforeStatement(result, getTestClass(), target);
        return result;
    }

    private Statement wrapBeforeStatement(Statement statement, TestClass testClass, Object target)
    {
        for (TestStatementDecoratorFactory statementHandler : this.statementDecoratorFactories)
        {
            Statement result = statementHandler.createBeforeStatement(statement, testClass, target);
            if (result != null)
            {
                statement = result;
            }
        }
        return statement;
    }

    //TODO use Rules instead
    @Override
    protected Statement withAfters(FrameworkMethod method,
                                   final Object target,
                                   final Statement statement)
    {
        Statement result = super.withAfters(method, target, statement);
        result = wrapAfterStatement(result, getTestClass(), target);
        return result;
    }

    private Statement wrapAfterStatement(Statement statement, TestClass testClass, Object target)
    {
        for (TestStatementDecoratorFactory statementHandler : this.statementDecoratorFactories)
        {
            Statement result = statementHandler.createAfterStatement(statement, testClass, target);
            if (result != null)
            {
                statement = result;
            }
        }
        return statement;
    }

    @Override
    protected Statement withBeforeClasses(Statement statement)
    {
        return new BeforeClassStatement(super.withBeforeClasses(statement),
            this.testContext,
            getTestClass().getJavaClass());
    }

    @Override
    protected Statement withAfterClasses(Statement statement)
    {
        Statement result = super.withAfterClasses(statement);
        if (!CdiTestSuiteRunner.isContainerStarted())
        {
            return new AfterClassStatement(result, this.testContext);
        }
        return result;
    }

    //TODO use Rules instead
    @Override
    protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next)
    {
        Statement result = super.withPotentialTimeout(method, test, next);

        if (result instanceof FailOnTimeout)
        {
            return new Statement()
            {
                @Override
                public void evaluate() throws Throwable
                {
                    throw new RuntimeException("@" + Test.class.getName() + "#timeout isn't supported");
                }
            };
        }

        return result;
    }

    private class ContainerAwareMethodInvoker extends Statement
    {
        private final FrameworkMethod method;
        private final Object originalTarget;

        public ContainerAwareMethodInvoker(FrameworkMethod method, Object originalTarget)
        {
            this.method = method;
            this.originalTarget = originalTarget;
        }

        @Override
        public void evaluate() throws Throwable
        {
            BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
            Class<?> type = this.method.getMethod().getDeclaringClass();
            Set<Bean<?>> beans = beanManager.getBeans(type);

            if (!USE_TEST_CLASS_AS_CDI_BEAN || beans == null || beans.isEmpty())
            {
                if (!ALLOW_INJECTION_POINT_MANIPULATION)
                {
                    BeanProvider.injectFields(this.originalTarget); //fallback to simple injection
                }
                invokeMethod(this.originalTarget);
            }
            else
            {
                Bean<Object> bean = (Bean<Object>) beanManager.resolve(beans);

                CreationalContext<Object> creationalContext = beanManager.createCreationalContext(bean);

                Object target = beanManager.getReference(bean, type, creationalContext);

                try
                {
                    invokeMethod(target);
                }
                finally
                {
                    if (bean.getScope().equals(Dependent.class))
                    {
                        bean.destroy(target, creationalContext);
                    }
                }
            }
        }

        private void invokeMethod(Object target)
        {
            try
            {
                this.method.invokeExplosively(target);
            }
            catch (Throwable throwable)
            {
                throw ExceptionUtils.throwAsRuntimeException(throwable);
            }
        }
    }

    private class BeforeClassStatement extends Statement
    {
        private final Statement wrapped;
        private final ContainerAwareTestContext testContext;
        private final Class testClass;

        BeforeClassStatement(Statement statement, ContainerAwareTestContext testContext, Class testClass)
        {
            this.wrapped = statement;
            this.testContext = testContext;
            this.testClass = testClass;
        }

        @Override
        public void evaluate() throws Throwable
        {
            testContext.applyBeforeClassConfig(this.testClass);
            wrapped.evaluate();
        }
    }

    private class AfterClassStatement extends Statement
    {
        private final Statement wrapped;
        private final ContainerAwareTestContext testContext;

        public AfterClassStatement(Statement statement,
                                   ContainerAwareTestContext testContext)
        {
            this.wrapped = statement;
            this.testContext = testContext;
        }

        @Override
        public void evaluate() throws Throwable
        {
            try
            {
                wrapped.evaluate();
            }
            finally
            {
                testContext.applyAfterClassConfig();
            }
        }
    }

    private static class ContainerAwareTestContext
    {
        private ContainerAwareTestContext parent;

        private final ProjectStage projectStage;
        private final TestControl testControl;

        private ProjectStage previousProjectStage;

        private boolean containerStarted = false; //only true for the layer it was started in

        private Stack<Class<? extends Annotation>> startedScopes = new Stack<Class<? extends Annotation>>();

        private List<ExternalContainer> externalContainers;

        ContainerAwareTestContext(TestControl testControl, ContainerAwareTestContext parent)
        {
            this.parent = parent;

            Class<? extends ProjectStage> foundProjectStageClass;
            if (testControl == null)
            {
                this.testControl = new TestControlLiteral();
                if (parent != null)
                {
                    foundProjectStageClass = parent.testControl.projectStage();
                }
                else
                {
                    foundProjectStageClass = this.testControl.projectStage();
                }
            }
            else
            {
                this.testControl = testControl;
                foundProjectStageClass = this.testControl.projectStage();
            }
            this.projectStage = ProjectStage.valueOf(foundProjectStageClass.getSimpleName());

            ProjectStageProducer.setProjectStage(this.projectStage);
        }

        boolean isContainerStarted()
        {
            return this.containerStarted || (this.parent != null && this.parent.isContainerStarted()) ||
                    CdiTestSuiteRunner.isContainerStarted();
        }

        Class<? extends Handler> getLogHandlerClass()
        {
            return this.testControl.logHandler();
        }

        void applyBeforeClassConfig(Class<?> testClass)
        {
            CdiContainer container = CdiContainerLoader.getCdiContainer();

            if (!isContainerStarted())
            {
                if (!CdiTestSuiteRunner.isContainerStarted())
                {
                    // We are setting this system property to make the deployment for Weld "flat"
                    // This (amongst other things) means that alternatives enabled via beans.xml will be 
                    // enabled globally
                    // Beginning with Weld 2.x you could use Weld.property(), but here we depend on Weld 1.x API
                    // Note that Weld 1 was "flat" anyway, so this property only affects newer versions of Weld
                    System.setProperty("org.jboss.weld.se.archive.isolation", "false");

                    CdiTestSuiteRunner.applyTestSpecificMetaData(testClass);

                    container.boot(CdiTestSuiteRunner.getTestContainerConfig());
                    setContainerStarted();

                    bootExternalContainers(testClass);
                }
            }

            List<Class<? extends Annotation>> restrictedScopes = new ArrayList<Class<? extends Annotation>>();

            //controlled by the container and not supported by weld:
            restrictedScopes.add(ApplicationScoped.class);
            restrictedScopes.add(Singleton.class);

            if (this.parent == null && this.testControl.getClass().equals(TestControlLiteral.class))
            {
                //skip scope-handling if @TestControl isn't used explicitly on the test-class -> TODO re-visit it
                restrictedScopes.add(RequestScoped.class);
                restrictedScopes.add(SessionScoped.class);
            }

            startScopes(container, testClass, null, restrictedScopes.toArray(new Class[restrictedScopes.size()]));
        }

        private void bootExternalContainers(Class testClass)
        {
            if (!this.testControl.startExternalContainers())
            {
                return;
            }

            if (this.externalContainers == null)
            {
                List<ExternalContainer> configuredExternalContainers =
                    ServiceUtils.loadServiceImplementations(ExternalContainer.class);
                Collections.sort(configuredExternalContainers, new Comparator<ExternalContainer>()
                {
                    @Override
                    public int compare(ExternalContainer ec1, ExternalContainer ec2)
                    {
                        return ec1.getOrdinal() > ec2.getOrdinal() ? 1 : -1;
                    }
                });

                this.externalContainers = new ArrayList<ExternalContainer>(configuredExternalContainers.size());

                ExternalContainer externalContainerBean;
                for (ExternalContainer externalContainer : configuredExternalContainers)
                {
                    //needed to use cdi-observers in the container optionally
                    externalContainerBean = BeanProvider.getContextualReference(externalContainer.getClass(), true);

                    if (externalContainerBean != null)
                    {
                        this.externalContainers.add(externalContainerBean);
                    }
                    else
                    {
                        this.externalContainers.add(externalContainer);
                    }
                }

                for (ExternalContainer externalContainer : this.externalContainers)
                {
                    try
                    {
                        if (externalContainer instanceof TestAware)
                        {
                            ((TestAware)externalContainer).setTestClass(testClass);
                        }
                        externalContainer.boot();
                    }
                    catch (RuntimeException e)
                    {
                        Logger.getLogger(CdiTestRunner.class.getName()).log(Level.WARNING,
                                "booting " + externalContainer.getClass().getName() + " failed", e);
                    }
                }
            }
        }

        void applyAfterClassConfig()
        {
            CdiContainer container = CdiContainerLoader.getCdiContainer();

            stopStartedScopes(container);

            if (this.containerStarted)
            {
                if (CdiTestSuiteRunner.isStopContainerAllowed())
                {
                    shutdownExternalContainers();

                    container.shutdown(); //stop the container on the same level which started it
                    CdiTestSuiteRunner.setContainerStarted(false);
                }
            }
        }

        private void shutdownExternalContainers()
        {
            if (this.externalContainers == null)
            {
                return;
            }

            for (ExternalContainer externalContainer : this.externalContainers)
            {
                try
                {
                    externalContainer.shutdown();
                }
                catch (RuntimeException e)
                {
                    Logger.getLogger(CdiTestRunner.class.getName()).log(Level.WARNING,
                            "shutting down " + externalContainer.getClass().getName() + " failed", e);
                }
            }
        }

        void applyBeforeMethodConfig(Method testMethod)
        {
            this.previousProjectStage = ProjectStageProducer.getInstance().getProjectStage();
            ProjectStageProducer.setProjectStage(this.projectStage);

            setCurrentTestMethod(testMethod);
            startScopes(CdiContainerLoader.getCdiContainer(), testMethod.getDeclaringClass(), testMethod);
        }

        void applyAfterMethodConfig()
        {
            try
            {
                stopStartedScopes(CdiContainerLoader.getCdiContainer());
            }
            finally
            {
                setCurrentTestMethod(null);
                ProjectStageProducer.setProjectStage(previousProjectStage);
                previousProjectStage = null;
                currentTestRunner.set(null);
                currentTestRunner.remove();
            }
        }

        void setContainerStarted()
        {
            this.containerStarted = true;
            CdiTestSuiteRunner.setContainerStarted(true);
        }

        private void startScopes(CdiContainer container,
                                 Class testClass,
                                 Method testMethod,
                                 Class<? extends Annotation>... restrictedScopes)
        {
            try
            {
                automaticScopeHandlingActive.set(TRUE);

                ContextControl contextControl = container.getContextControl();

                List<Class<? extends Annotation>> scopeClasses = new ArrayList<Class<? extends Annotation>>();

                Collections.addAll(scopeClasses, this.testControl.startScopes());

                if (scopeClasses.isEmpty())
                {
                    addScopesForDefaultBehavior(scopeClasses);
                }
                else
                {
                    List<TestControlValidator> testControlValidatorList =
                        ServiceUtils.loadServiceImplementations(TestControlValidator.class);

                    for (TestControlValidator testControlValidator : testControlValidatorList)
                    {
                        if (testControlValidator instanceof TestAware)
                        {
                            if (testMethod != null)
                            {
                                ((TestAware)testControlValidator).setTestMethod(testMethod);
                            }
                            ((TestAware)testControlValidator).setTestClass(testClass);
                        }
                        try
                        {
                            testControlValidator.validate(this.testControl);
                        }
                        finally
                        {
                            if (testControlValidator instanceof TestAware)
                            {
                                ((TestAware)testControlValidator).setTestClass(null);
                                ((TestAware)testControlValidator).setTestMethod(null);
                            }
                        }
                    }
                }

                for (Class<? extends Annotation> scopeAnnotation : scopeClasses)
                {
                    if (this.parent != null && this.parent.isScopeStarted(scopeAnnotation))
                    {
                        continue;
                    }

                    if (isRestrictedScope(scopeAnnotation, restrictedScopes))
                    {
                        continue;
                    }

                    try
                    {
                        //force a clean context - TODO discuss onScopeStopped call
                        contextControl.stopContext(scopeAnnotation);

                        contextControl.startContext(scopeAnnotation);
                        this.startedScopes.add(scopeAnnotation);

                        onScopeStarted(scopeAnnotation);
                    }
                    catch (RuntimeException e)
                    {
                        Logger logger = Logger.getLogger(CdiTestRunner.class.getName());
                        logger.setLevel(Level.SEVERE);
                        logger.log(Level.SEVERE, "failed to start scope @" + scopeAnnotation.getName(), e);
                    }
                }
            }
            finally
            {
                automaticScopeHandlingActive.set(null);
                automaticScopeHandlingActive.remove();
            }
        }

        private void addScopesForDefaultBehavior(List<Class<? extends Annotation>> scopeClasses)
        {
            if (this.parent != null && !this.parent.isScopeStarted(RequestScoped.class))
            {
                if (!scopeClasses.contains(RequestScoped.class))
                {
                    scopeClasses.add(RequestScoped.class);
                }
            }
            if (this.parent != null && !this.parent.isScopeStarted(SessionScoped.class))
            {
                if (!scopeClasses.contains(SessionScoped.class))
                {
                    scopeClasses.add(SessionScoped.class);
                }
            }
        }

        private boolean isRestrictedScope(Class<? extends Annotation> scopeAnnotation,
                                          Class<? extends Annotation>[] restrictedScopes)
        {
            for (Class<? extends Annotation> restrictedScope : restrictedScopes)
            {
                if (scopeAnnotation.equals(restrictedScope))
                {
                    return true;
                }
            }
            return false;
        }

        private boolean isScopeStarted(Class<? extends Annotation> scopeAnnotation)
        {
            return this.startedScopes.contains(scopeAnnotation);
        }

        private void stopStartedScopes(CdiContainer container)
        {
            try
            {
                automaticScopeHandlingActive.set(TRUE);

                while (!this.startedScopes.empty())
                {
                    Class<? extends Annotation> scopeAnnotation = this.startedScopes.pop();
                    //TODO check if context was started by parent
                    try
                    {
                        container.getContextControl().stopContext(scopeAnnotation);
                        onScopeStopped(scopeAnnotation);
                    }
                    catch (RuntimeException e)
                    {
                        Logger logger = Logger.getLogger(CdiTestRunner.class.getName());
                        logger.setLevel(Level.SEVERE);
                        logger.log(Level.SEVERE, "failed to stop scope @" + scopeAnnotation.getName(), e);
                    }
                }
            }
            finally
            {
                automaticScopeHandlingActive.remove();
                automaticScopeHandlingActive.set(null);
            }
        }

        private void onScopeStarted(Class<? extends Annotation> scopeClass)
        {
            List<ExternalContainer> externalContainerList = collectExternalContainers(this);

            for (ExternalContainer externalContainer : externalContainerList)
            {
                externalContainer.startScope(scopeClass);
            }
        }

        private void onScopeStopped(Class<? extends Annotation> scopeClass)
        {
            List<ExternalContainer> externalContainerList = collectExternalContainers(this);

            for (ExternalContainer externalContainer : externalContainerList)
            {
                externalContainer.stopScope(scopeClass);
            }
        }

        private static List<ExternalContainer> collectExternalContainers(ContainerAwareTestContext testContext)
        {
            List<ExternalContainer> result = new ArrayList<ExternalContainer>();

            if (testContext.externalContainers != null)
            {
                result.addAll(testContext.externalContainers);
            }

            if (testContext.parent != null)
            {
                result.addAll(collectExternalContainers(testContext.parent));
            }
            return result;
        }

        private void setCurrentTestMethod(Method testMethod)
        {
            List<ExternalContainer> externalContainerList = collectExternalContainers(this);

            for (ExternalContainer externalContainer : externalContainerList)
            {
                if (externalContainer instanceof TestAware)
                {
                    try
                    {
                        ((TestAware)externalContainer).setTestMethod(testMethod);
                    }
                    catch (Throwable t)
                    {
                        //with a correct setup it shouldn't happen
                        //TODO better handling for invalid constellations
                        Assert.fail(t.getMessage());
                    }
                }
            }
        }
    }

    public static Boolean isAutomaticScopeHandlingActive()
    {
        return automaticScopeHandlingActive.get();
    }

    public static List<ExternalContainer> getActiveExternalContainers()
    {
        CdiTestRunner cdiTestRunner = currentTestRunner.get();

        if (cdiTestRunner == null ||
            cdiTestRunner.testContext == null ||
            cdiTestRunner.testContext.externalContainers == null)
        {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(cdiTestRunner.testContext.externalContainers);
    }

}
