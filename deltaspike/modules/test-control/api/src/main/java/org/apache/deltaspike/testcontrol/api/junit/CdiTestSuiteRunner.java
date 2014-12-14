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
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static volatile boolean containerStarted; //TODO

    private final Class<?> testSuiteClass;

    static
    {
        STOP_CONTAINER = TestBaseConfig.Container.STOP_CONTAINER.getValue();
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
            this.logger.info("[run] " + description.getClassName() + "#" + description.getMethodName());
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
            this.logger.info("[finished] " + description.getClassName() + "#" + description.getMethodName());
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
            Description description = failure.getDescription();
            this.logger.info("[failed] " + description.getClassName() + "#" + description.getMethodName() +
                    " message: " + failure.getMessage());
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
}
