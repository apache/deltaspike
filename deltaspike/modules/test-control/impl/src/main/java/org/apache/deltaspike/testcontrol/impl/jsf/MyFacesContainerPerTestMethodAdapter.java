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

import org.apache.deltaspike.testcontrol.spi.ExternalContainer;
import org.apache.deltaspike.testcontrol.spi.TestAware;

import javax.enterprise.context.ApplicationScoped;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Optional adapter for MyFacesContainer which gets started and stopped for every test-method
 * Requires MyFaces-Core 2.2.x, MyFaces-Test v1.0.6 or higher as well as org.apache.myfaces.core:myfaces-impl-test
 */
@ApplicationScoped
public class MyFacesContainerPerTestMethodAdapter implements TestAware, ExternalContainer
{
    private static ThreadLocal<MyFacesContainerAdapter> myFacesContainerAdapterThreadLocal =
            new ThreadLocal<MyFacesContainerAdapter>();

    @Override
    public void boot()
    {
    }

    @Override
    public void shutdown()
    {
    }

    @Override
    public int getOrdinal()
    {
        return 1000; //default in ds
    }

    @Override
    public void startScope(Class<? extends Annotation> scopeClass)
    {
        myFacesContainerAdapterThreadLocal.get().startScope(scopeClass);
    }

    @Override
    public void stopScope(Class<? extends Annotation> scopeClass)
    {
        myFacesContainerAdapterThreadLocal.get().stopScope(scopeClass);
    }

    @Override
    public void setTestClass(Class testClass)
    {
        MyFacesContainerAdapter myFacesContainerAdapter = new MyFacesContainerAdapter();
        myFacesContainerAdapter.setTestClass(testClass);
        myFacesContainerAdapterThreadLocal.set(myFacesContainerAdapter);
    }

    @Override
    public void setTestMethod(Method testMethod)
    {
        if (testMethod != null)
        {
            myFacesContainerAdapterThreadLocal.get().boot();
        }
        else
        {
            myFacesContainerAdapterThreadLocal.get().shutdown();
        }
    }
}
