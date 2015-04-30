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
package org.apache.deltaspike.testcontrol.impl.jsf;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.testcontrol.spi.ExternalContainer;
import org.apache.deltaspike.testcontrol.spi.TestAware;
import org.apache.myfaces.mc.test.core.annotation.TestConfig;
import org.apache.myfaces.mc.test.core.runner.MyFacesContainer;
import org.junit.runners.model.TestClass;

import javax.el.ExpressionFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Optional adapter for MyFacesContainer
 * Requires MyFaces-Core 2.2.x, MyFaces-Test v1.0.6 or higher as well as org.apache.myfaces.core:myfaces-impl-test
 */
@ApplicationScoped
public class MyFacesContainerAdapter implements TestAware, ExternalContainer
{
    private static final TestConfig DEFAULT_TEST_CONFIG_LITERAL = AnnotationInstanceProvider.of(TestConfig.class);
    protected MyFacesContainer mockedMyFacesTestContainer;
    protected Class testClass;
    protected Map<String, String> containerConfig = new HashMap<String, String>();

    public void boot()
    {
        final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        this.mockedMyFacesTestContainer = new MyFacesContainer(new TestClass(this.testClass))
        {
            @Override
            protected String getWebappResourcePath()
            {
                TestConfig testConfig = testClass.getJavaClass().getAnnotation(TestConfig.class);

                if (testConfig == null || DEFAULT_TEST_CONFIG_LITERAL.webappResourcePath().equals(
                    testConfig.webappResourcePath()))
                {
                    return MyFacesTestBaseConfig.WEBAPP_RESOURCE_PATH;
                }
                return testConfig.webappResourcePath();
            }

            @Override
            protected void setUpServletObjects()
            {
                //just needed for MyFaces-Test util v1.0.7
                //(to bypass issues with the outdated URLClassLoader used by AbstractJsfTestContainer)
                setCurrentClassLoader(originalClassLoader);
                super.setUpServletObjects();
            }

            @Override
            protected void setUpWebConfigParams()
            {
                servletContext.addInitParameter("org.apache.myfaces.config.annotation.LifecycleProvider",
                    "org.apache.myfaces.config.annotation.NoInjectionAnnotationLifecycleProvider");
                servletContext.addInitParameter("org.apache.myfaces.CHECKED_VIEWID_CACHE_ENABLED", "false");

                servletContext.addInitParameter(ExpressionFactory.class.getName(),
                    "org.apache.el.ExpressionFactoryImpl");

                super.setUpWebConfigParams();

                initContainerConfig();

                //add custom values (might replace the default values)
                for (Map.Entry<String, String> entry : containerConfig.entrySet())
                {
                    servletContext.addInitParameter(entry.getKey(), entry.getValue());
                }
            }
        };

        this.mockedMyFacesTestContainer.setUp(new Object() /*we don't need the test-instance here*/);
    }

    protected void setCurrentClassLoader(ClassLoader originalClassLoader)
    {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    protected void initContainerConfig()
    {
        containerConfig = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : ConfigResolver.getAllProperties().entrySet())
        {
            if (entry.getKey().startsWith("org.apache.myfaces.") || entry.getKey().startsWith("javax.faces.") ||
                    entry.getKey().startsWith("facelets."))
            {
                containerConfig.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void startScope(Class<? extends Annotation> scopeClass)
    {
        if (RequestScoped.class.equals(scopeClass))
        {
            this.mockedMyFacesTestContainer.startRequest();
        }
    }

    @Override
    public void stopScope(Class<? extends Annotation> scopeClass)
    {
        if (RequestScoped.class.equals(scopeClass))
        {
            this.mockedMyFacesTestContainer.endRequest();
        }
    }

    public void shutdown()
    {
        if (this.mockedMyFacesTestContainer == null)
        {
            throw new IllegalStateException("During starting MyFaces-Core an exception happened.");
        }
        this.mockedMyFacesTestContainer.tearDown();
    }

    @Override
    public int getOrdinal()
    {
        return 1000; //default in ds
    }

    @Override
    public void setTestClass(Class testClass)
    {
        this.testClass = testClass;
    }

    @Override
    public void setTestMethod(Method method)
    {
        //not needed
    }
}
