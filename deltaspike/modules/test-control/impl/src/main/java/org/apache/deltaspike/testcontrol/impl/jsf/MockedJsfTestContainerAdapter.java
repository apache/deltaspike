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

    private final Method containerStartMethod;
    private final Method containerStopMethod;

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
            this.containerStartMethod = this.mockedMyFacesTestContainer.getClass().getDeclaredMethod("setUp");
            this.containerStopMethod = this.mockedMyFacesTestContainer.getClass().getDeclaredMethod("tearDown");
        }
        catch (NoSuchMethodException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    public void boot()
    {
        //MockedJsfTestContainer needs to be bootstrapped for every request
    }

    @Override
    public void startScope(Class<? extends Annotation> scopeClass)
    {
        if (RequestScoped.class.equals(scopeClass))
        {
            try
            {
                //see the comment at #boot
                this.containerStartMethod.invoke(this.mockedMyFacesTestContainer);
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
                //see the comment at #shutdown
                this.containerStopMethod.invoke(this.mockedMyFacesTestContainer);
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }
    }

    public void shutdown()
    {
        //MockedJsfTestContainer needs to be stopped after every request
    }

    @Override
    public int getOrdinal()
    {
        return 1000; //default in ds
    }
}
