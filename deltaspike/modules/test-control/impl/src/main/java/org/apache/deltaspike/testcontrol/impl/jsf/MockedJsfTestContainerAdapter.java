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

import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.testcontrol.spi.ExternalContainer;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Optional adapter for MockedJsfTestContainer
 * Requires MyFaces-Test v1.0.6 or higher
 */
//TODO use MockedJsfTestContainer without reflection once v1.0.6 of myfaces-test is released
public class MockedJsfTestContainerAdapter implements ExternalContainer
{
    private final Object mockedMyFacesTestContainer;

    private final Method startContainerMethod;
    private final Method stopContainerMethod;

    private final Method startRequestMethod;
    private final Method stopRequestMethod;

    private final Method startSessionMethod;
    private final Method stopSessionMethod;

    public MockedJsfTestContainerAdapter()
    {
        this.mockedMyFacesTestContainer =
            ClassUtils.tryToInstantiateClassForName("org.apache.myfaces.test.mock.MockedJsfTestContainer");

        if (this.mockedMyFacesTestContainer == null)
        {
            throw new IllegalStateException("This adapter requires MyFaces-Test v1.0.6 or higher.");
        }

        try
        {
            this.startContainerMethod = this.mockedMyFacesTestContainer.getClass().getDeclaredMethod("setUp");
            this.stopContainerMethod = this.mockedMyFacesTestContainer.getClass().getDeclaredMethod("tearDown");

            this.startRequestMethod = this.mockedMyFacesTestContainer.getClass().getDeclaredMethod("startRequest");
            this.stopRequestMethod = this.mockedMyFacesTestContainer.getClass().getDeclaredMethod("endRequest");

            this.startSessionMethod = this.mockedMyFacesTestContainer.getClass().getDeclaredMethod("startSession");
            this.stopSessionMethod = this.mockedMyFacesTestContainer.getClass().getDeclaredMethod("endSession");
        }
        catch (NoSuchMethodException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    public void boot()
    {
        try
        {
            this.startContainerMethod.invoke(this.mockedMyFacesTestContainer);
        }
        catch (Exception e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public void startScope(Class<? extends Annotation> scopeClass)
    {
        if (RequestScoped.class.equals(scopeClass))
        {
            try
            {
                this.startRequestMethod.invoke(this.mockedMyFacesTestContainer);
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }
        else if (SessionScoped.class.equals(scopeClass))
        {
            try
            {
                this.startSessionMethod.invoke(this.mockedMyFacesTestContainer);
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }
    }

    @Override
    public void stopScope(Class<? extends Annotation> scopeClass)
    {
        if (RequestScoped.class.equals(scopeClass))
        {
            try
            {
                this.stopRequestMethod.invoke(this.mockedMyFacesTestContainer);
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }
        else if (SessionScoped.class.equals(scopeClass))
        {
            try
            {
                this.stopSessionMethod.invoke(this.mockedMyFacesTestContainer);
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }
    }

    public void shutdown()
    {
        try
        {
            this.stopContainerMethod.invoke(this.mockedMyFacesTestContainer);
        }
        catch (Exception e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public int getOrdinal()
    {
        return 1000; //default in ds
    }
}
