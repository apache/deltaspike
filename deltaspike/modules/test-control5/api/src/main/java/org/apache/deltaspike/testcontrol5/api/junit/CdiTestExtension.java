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
package org.apache.deltaspike.testcontrol5.api.junit;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.core.util.ServiceUtils;
import org.apache.deltaspike.testcontrol5.api.TestControl;
import org.apache.deltaspike.testcontrol5.api.literal.TestControlLiteral;
import org.apache.deltaspike.testcontrol5.spi.ExternalContainer;
import org.apache.deltaspike.testcontrol5.spi.TestAware;
import org.apache.deltaspike.testcontrol5.spi.TestControlValidator;
import org.apache.deltaspike.testcontrol5.spi.junit.TestStatementDecoratorFactory;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Singleton;

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


/**
 * A JUnit 5 extension to start up with a CDI or embedded JavaEE container.
 */
public class CdiTestExtension implements BeforeAllCallback, AfterAllCallback,
        BeforeEachCallback, AfterEachCallback, ParameterResolver
{
    private static final Logger LOGGER = Logger.getLogger(CdiTestExtension.class.getName());

    private static final boolean USE_TEST_CLASS_AS_CDI_BEAN;
    private static final boolean ALLOW_INJECTION_POINT_MANIPULATION;

    private static Set<Integer> extensionIdentities = new CopyOnWriteArraySet<Integer>();

    static
    {
        USE_TEST_CLASS_AS_CDI_BEAN = TestBaseConfig.ContainerIntegration.USE_TEST_CLASS_AS_CDI_BEAN;
        ALLOW_INJECTION_POINT_MANIPULATION = TestBaseConfig.MockIntegration.ALLOW_MANUAL_INJECTION_POINT_MANIPULATION;
    }

    private static ThreadLocal<Boolean> automaticScopeHandlingActive = new ThreadLocal<Boolean>();

    private static ThreadLocal<CdiTestExtension> currentTestExtension = new ThreadLocal<CdiTestExtension>();

    private List<TestStatementDecoratorFactory> statementDecoratorFactories;

    private ContainerAwareTestContext testContext;

    public CdiTestExtension()
    {
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception
    {
        currentTestExtension.set(this);

        TestControl testControl = extensionContext.getTestClass()
                .map(cls -> cls.getAnnotation(TestControl.class)).orElse(null);

        ContainerAwareTestContext currentTestContext =
                new ContainerAwareTestContext(testControl, this.testContext);

        extensionContext.getTestMethod().ifPresent(method ->
            {
                try
                {
                    currentTestContext.applyBeforeMethodConfig(method);
                }
                catch (Exception e)
                {
                    throw ExceptionUtils.throwAsRuntimeException(e);
                }
            });

        this.testContext = currentTestContext;
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception
    {
        try
        {
            if (this.testContext != null)
            {
                this.testContext.applyAfterMethodConfig();
            }
        }
        finally
        {
            currentTestExtension.set(null);
            currentTestExtension.remove();
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception
    {
        if (this.testContext == null)
        {
            TestControl testControl = extensionContext.getTestClass()
                    .map(cls -> cls.getAnnotation(TestControl.class)).orElse(null);

            this.testContext = new ContainerAwareTestContext(testControl, null);

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

        this.testContext.applyBeforeClassConfig(extensionContext.getTestClass().orElseThrow());
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception
    {
        if (this.testContext != null)
        {
            this.testContext.applyAfterClassConfig();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException
    {
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException
    {
        return null;
    }

    private class ContainerAwareTestContext
    {
        private ContainerAwareTestContext parent;

        private final ProjectStage projectStage;
        private final TestControl testControl;

        private ProjectStage previousProjectStage;

        private boolean containerStarted = false;

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
            return this.containerStarted || (this.parent != null && this.parent.isContainerStarted());
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
                System.setProperty("org.jboss.weld.environment.servlet.archive.isolation", "false");

                container.boot(CdiTestSuiteExtension.getTestContainerConfig());
                setContainerStarted();

                bootExternalContainers(testClass);
            }

            List<Class<? extends Annotation>> restrictedScopes = new ArrayList<Class<? extends Annotation>>();

            restrictedScopes.add(ApplicationScoped.class);
            restrictedScopes.add(Singleton.class);

            if (this.parent == null && this.testControl.getClass().equals(TestControlLiteral.class))
            {
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
                            ((TestAware) externalContainer).setTestClass(testClass);
                        }
                        externalContainer.boot();
                    }
                    catch (RuntimeException e)
                    {
                        Logger.getLogger(CdiTestExtension.class.getName()).log(Level.WARNING,
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
                if (CdiTestSuiteExtension.isStopContainerAllowed())
                {
                    shutdownExternalContainers();

                    container.shutdown();
                    CdiTestSuiteExtension.setContainerStarted(false);
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
                    Logger.getLogger(CdiTestExtension.class.getName()).log(Level.WARNING,
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
            }
        }

        void setContainerStarted()
        {
            this.containerStarted = true;
            CdiTestSuiteExtension.setContainerStarted(true);
        }

        private void startScopes(CdiContainer container,
                                 Class testClass,
                                 Method testMethod,
                                 Class<? extends Annotation>... restrictedScopes)
        {
            try
            {
                automaticScopeHandlingActive.set(true);

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
                                ((TestAware) testControlValidator).setTestMethod(testMethod);
                            }
                            ((TestAware) testControlValidator).setTestClass(testClass);
                        }
                        try
                        {
                            testControlValidator.validate(this.testControl);
                        }
                        finally
                        {
                            if (testControlValidator instanceof TestAware)
                            {
                                ((TestAware) testControlValidator).setTestClass(null);
                                ((TestAware) testControlValidator).setTestMethod(null);
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
                        contextControl.stopContext(scopeAnnotation);

                        contextControl.startContext(scopeAnnotation);
                        this.startedScopes.add(scopeAnnotation);

                        onScopeStarted(scopeAnnotation);
                    }
                    catch (RuntimeException e)
                    {
                        Logger logger = Logger.getLogger(CdiTestExtension.class.getName());
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
                automaticScopeHandlingActive.set(true);

                while (!this.startedScopes.empty())
                {
                    Class<? extends Annotation> scopeAnnotation = this.startedScopes.pop();
                    try
                    {
                        container.getContextControl().stopContext(scopeAnnotation);
                        onScopeStopped(scopeAnnotation);
                    }
                    catch (RuntimeException e)
                    {
                        Logger logger = Logger.getLogger(CdiTestExtension.class.getName());
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

        private List<ExternalContainer> collectExternalContainers(ContainerAwareTestContext testContext)
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
                        ((TestAware) externalContainer).setTestMethod(testMethod);
                    }
                    catch (Throwable t)
                    {
                        throw new RuntimeException(t.getMessage());
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
        CdiTestExtension cdiTestExtension = currentTestExtension.get();

        if (cdiTestExtension == null ||
                cdiTestExtension.testContext == null ||
                cdiTestExtension.testContext.externalContainers == null)
        {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(cdiTestExtension.testContext.externalContainers);
    }

}
