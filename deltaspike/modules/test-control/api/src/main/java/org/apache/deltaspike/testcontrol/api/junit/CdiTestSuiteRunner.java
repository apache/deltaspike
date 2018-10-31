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

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.PropertyLoader;
import org.apache.deltaspike.core.spi.filter.ClassFilter;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Boolean.TRUE;

@SuppressWarnings("UnusedDeclaration")
public class CdiTestSuiteRunner extends Suite
{
    /**
     * Configuration key to define a custom configuration properties file.
     * e.g.:
     * deltaspike.testcontrol.test-container.config-file=META-INF/dsTestContainerBootConfig.properties
     */
    public static final String CUSTOM_TEST_CONTAINER_CONFIG_FILE_KEY =
        "deltaspike.testcontrol.test-container.config-file";

    /**
     * Default resource location of the property file which gets used
     * for the container bootstrap.
     * This value can be overridden by using {@link #CUSTOM_TEST_CONTAINER_CONFIG_FILE_KEY}
     */
    public static final String DEFAULT_TEST_CONTAINER_CONFIG_FILE_NAME =
        "META-INF/apache-deltaspike_test-container";

    private static final boolean STOP_CONTAINER;

    private static final ThreadLocal<Boolean> IS_CDI_TEST_RUNNER_EXECUTION = new ThreadLocal<Boolean>();

    private static volatile boolean containerStarted; //TODO

    private final Class<?> testSuiteClass;

    static
    {
        STOP_CONTAINER = TestBaseConfig.ContainerIntegration.STOP_CONTAINER;
    }

    public CdiTestSuiteRunner(Class<?> klass, RunnerBuilder builder) throws InitializationError
    {
        super(klass, builder);
        this.testSuiteClass = klass;
    }

    protected CdiTestSuiteRunner(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError
    {
        super(klass, suiteClasses);
        this.testSuiteClass = klass;
    }

    protected CdiTestSuiteRunner(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses)
        throws InitializationError
    {
        super(builder, klass, suiteClasses);
        this.testSuiteClass = klass;
    }

    protected CdiTestSuiteRunner(Class<?> klass, List<Runner> runners) throws InitializationError
    {
        super(klass, runners);
        this.testSuiteClass = klass;
    }

    @Override
    public void run(RunNotifier notifier)
    {
        if (this.testSuiteClass == null)
        {
            throw new IllegalStateException("no test-suite class found");
        }

        CdiContainer container = CdiContainerLoader.getCdiContainer();

        if (!containerStarted)
        {
            applyTestSpecificMetaData(getTestClass().getJavaClass());

            container.boot(getTestContainerConfig());
            containerStarted = true;
        }

        notifier.addListener(new LogRunListener());

        try
        {
            super.run(notifier);
        }
        finally
        {
            if (STOP_CONTAINER)
            {
                container.shutdown();
                containerStarted = false;
            }
        }
    }


    public static boolean isContainerStarted()
    {
        return containerStarted;
    }

    static Boolean isStopContainerAllowed()
    {
        return STOP_CONTAINER;
    }

    static ThreadLocal<Boolean> getCdiTestRunnerExecutionRef()
    {
        return IS_CDI_TEST_RUNNER_EXECUTION;
    }

    static void setContainerStarted(boolean containerStarted)
    {
        CdiTestSuiteRunner.containerStarted = containerStarted;
    }

    static class LogRunListener extends RunListener
    {
        private final Logger logger = Logger.getLogger(LogRunListener.class.getName());

        LogRunListener()
        {
        }

        @Override
        public void testStarted(Description description) throws Exception
        {
            Level level = this.logger.getLevel();

            this.logger.setLevel(Level.INFO);
            if (TRUE.equals(IS_CDI_TEST_RUNNER_EXECUTION.get()))
            {
                this.logger.info("[run] " + description.getClassName() + "#" + description.getMethodName());
            }

            try
            {
                super.testRunStarted(description);
            }
            finally
            {
                this.logger.setLevel(level);
            }
        }

        @Override
        public void testFinished(Description description) throws Exception
        {
            Level level = this.logger.getLevel();

            this.logger.setLevel(Level.INFO);

            if (TRUE.equals(IS_CDI_TEST_RUNNER_EXECUTION.get()))
            {
                this.logger.info("[finished] " + description.getClassName() + "#" + description.getMethodName());
            }

            try
            {
                super.testFinished(description);
            }
            finally
            {
                this.logger.setLevel(level);
            }
        }

        @Override
        public void testFailure(Failure failure) throws Exception
        {
            Level level = this.logger.getLevel();

            this.logger.setLevel(Level.INFO);

            if (TRUE.equals(IS_CDI_TEST_RUNNER_EXECUTION.get()))
            {
                Description description = failure.getDescription();
                this.logger.info("[failed] " + description.getClassName() + "#" + description.getMethodName() +
                    " message: " + failure.getMessage());
            }

            try
            {
                super.testFailure(failure);
            }
            finally
            {
                this.logger.setLevel(level);
            }
        }
    }

    public static Properties getTestContainerConfig()
    {
        String cdiTestRunnerConfig = ConfigResolver.getProjectStageAwarePropertyValue(
            CUSTOM_TEST_CONTAINER_CONFIG_FILE_KEY, DEFAULT_TEST_CONTAINER_CONFIG_FILE_NAME);
        return PropertyLoader.getProperties(cdiTestRunnerConfig);
    }

    //just here, because all shared methods are in this class
    static void applyTestSpecificMetaData(Class<?> currentAnnotationSource)
    {
        TestControl testControl = currentAnnotationSource.getAnnotation(TestControl.class);
        String activeAlternativeLabel = checkForLabeledAlternativeConfig(testControl);

        initTestEnvConfig(currentAnnotationSource, activeAlternativeLabel, testControl);
    }

    private static String checkForLabeledAlternativeConfig(TestControl testControl)
    {
        String activeAlternativeLabel = "";

        if (testControl != null)
        {
            Class<? extends TestControl.Label> activeTypedAlternativeLabel =
                    testControl.activeAlternativeLabel();

            if (!TestControl.Label.class.equals(activeTypedAlternativeLabel))
            {
                Named labelName = activeTypedAlternativeLabel.getAnnotation(Named.class);

                if (labelName != null)
                {
                    activeAlternativeLabel = labelName.value();
                }
                else
                {
                    String labelClassName = activeTypedAlternativeLabel.getSimpleName();
                    activeAlternativeLabel = labelClassName.substring(0, 1).toLowerCase();

                    if (labelClassName.length() > 1)
                    {
                        activeAlternativeLabel += labelClassName.substring(1);
                    }
                }
            }
        }
        return activeAlternativeLabel;
    }

    private static void initTestEnvConfig(Class<?> testClass, String activeAlternativeLabel, TestControl testControl)
    {
        if (ClassDeactivationUtils.isActivated(TestConfigSource.class))
        {
            TestConfigSource testConfigSource = null;

            for (ConfigSource configSource : ConfigResolver.getConfigSources())
            {
                if (configSource instanceof TestConfigSource)
                {
                    //if it happens: parallel test-execution can't be supported with labeled alternatives
                    testConfigSource = (TestConfigSource) configSource;
                }
            }

            if (testConfigSource == null)
            {
                testConfigSource = new TestConfigSource();
                ConfigResolver.addConfigSources(Arrays.<ConfigSource>asList(testConfigSource));
            }

            //always set it even if it is empty (it might overrule the value of the prev. test
            testConfigSource.getProperties().put("activeAlternativeLabel", activeAlternativeLabel);

            testConfigSource.getProperties().put("activeAlternativeLabelSource", testClass.getName());

            if (testControl != null)
            {
                testConfigSource.getProperties().put(TestControl.class.getName(), testClass.getName());
                testConfigSource.getProperties().put(ClassFilter.class.getName(), testControl.classFilter().getName());
            }
            else
            {
                //reset it to avoid leaks between tests
                testConfigSource.getProperties().put(TestControl.class.getName(), TestControl.class.getName());
                testConfigSource.getProperties().put(ClassFilter.class.getName(), ClassFilter.class.getName());
            }
        }
        else
        {
            //always set it even if it is empty (it might overrule the value of the prev. test
            System.setProperty("activeAlternativeLabel", activeAlternativeLabel); //will be picked up by ds-core

            System.setProperty("activeAlternativeLabelSource", testClass.getName()); //can be used for custom logic

            if (testControl != null) //can be used for custom logic
            {
                System.setProperty(TestControl.class.getName(), testClass.getName());
                System.setProperty(ClassFilter.class.getName(), testControl.classFilter().getName());
            }
            else
            {
                //reset it to avoid leaks between tests
                System.setProperty(TestControl.class.getName(), TestControl.class.getName());
                System.setProperty(ClassFilter.class.getName(), ClassFilter.class.getName());
            }
        }
    }

    //config-sources are already stored per classloader
    //keep it public to allow type-safe deactivation (if needed)
    public static class TestConfigSource implements ConfigSource, Deactivatable
    {
        private Map<String, String> testConfig = new ConcurrentHashMap<String, String>();

        @Override
        public int getOrdinal()
        {
            return Integer.MIN_VALUE;
        }

        @Override
        public Map<String, String> getProperties()
        {
            return testConfig;
        }

        @Override
        public String getPropertyValue(String key)
        {
            return testConfig.get(key);
        }

        @Override
        public String getConfigName()
        {
            return "ds-test-config";
        }

        @Override
        public boolean isScannable()
        {
            return true;
        }
    }
}
