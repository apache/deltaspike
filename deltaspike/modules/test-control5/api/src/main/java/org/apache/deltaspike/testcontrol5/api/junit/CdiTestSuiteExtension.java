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
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.PropertyLoader;
import org.apache.deltaspike.core.spi.filter.ClassFilter;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.testcontrol5.api.TestControl;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import jakarta.inject.Named;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


public class CdiTestSuiteExtension implements BeforeAllCallback, AfterAllCallback
{
    public static final String CUSTOM_TEST_CONTAINER_CONFIG_FILE_KEY =
            "deltaspike.testcontrol.test-container.config-file";

    public static final String DEFAULT_TEST_CONTAINER_CONFIG_FILE_NAME =
            "META-INF/apache-deltaspike_test-container";

    private static final boolean STOP_CONTAINER;

    private static final ThreadLocal<Boolean> IS_CDI_TEST_RUNNER_EXECUTION = new ThreadLocal<>();

    private static volatile boolean containerStarted;

    private Class<?> testSuiteClass;

    static
    {
        STOP_CONTAINER = TestBaseConfig.ContainerIntegration.STOP_CONTAINER;
    }

    public CdiTestSuiteExtension()
    {
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception
    {
        this.testSuiteClass = extensionContext.getTestClass()
                .orElseThrow(() -> new IllegalStateException("no test-suite class found"));

        CdiContainer container = CdiContainerLoader.getCdiContainer();

        if (!containerStarted)
        {
            applyTestSpecificMetaData(testSuiteClass);

            container.boot(getTestContainerConfig());
            containerStarted = true;
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception
    {
        // Cleanup if needed
        if (STOP_CONTAINER)
        {
            CdiContainer container = CdiContainerLoader.getCdiContainer();

            container.shutdown();
            containerStarted = false;
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
        CdiTestSuiteExtension.containerStarted = containerStarted;
    }

    public static Properties getTestContainerConfig()
    {
        String cdiTestRunnerConfig = ConfigResolver.getProjectStageAwarePropertyValue(
                CUSTOM_TEST_CONTAINER_CONFIG_FILE_KEY, DEFAULT_TEST_CONTAINER_CONFIG_FILE_NAME);
        return PropertyLoader.getProperties(cdiTestRunnerConfig);
    }

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
                    testConfigSource = (TestConfigSource) configSource;
                }
            }

            if (testConfigSource == null)
            {
                testConfigSource = new TestConfigSource();
                ConfigResolver.addConfigSources(Arrays.<ConfigSource>asList(testConfigSource));
            }

            testConfigSource.getProperties().put("activeAlternativeLabel", activeAlternativeLabel);

            testConfigSource.getProperties().put("activeAlternativeLabelSource", testClass.getName());

            if (testControl != null)
            {
                testConfigSource.getProperties().put(TestControl.class.getName(), testClass.getName());
                testConfigSource.getProperties().put(ClassFilter.class.getName(), testControl.classFilter().getName());
            }
            else
            {
                testConfigSource.getProperties().put(TestControl.class.getName(), TestControl.class.getName());
                testConfigSource.getProperties().put(ClassFilter.class.getName(), ClassFilter.class.getName());
            }
        }
        else
        {
            System.setProperty("activeAlternativeLabel", activeAlternativeLabel);

            System.setProperty("activeAlternativeLabelSource", testClass.getName());

            if (testControl != null)
            {
                System.setProperty(TestControl.class.getName(), testClass.getName());
                System.setProperty(ClassFilter.class.getName(), testControl.classFilter().getName());
            }
            else
            {
                System.setProperty(TestControl.class.getName(), TestControl.class.getName());
                System.setProperty(ClassFilter.class.getName(), ClassFilter.class.getName());
            }
        }
    }

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
            return "ds-test5-config";
        }

        @Override
        public boolean isScannable()
        {
            return true;
        }
    }
}
