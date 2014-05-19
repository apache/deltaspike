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
package org.apache.deltaspike.test.testcontrol;

import org.apache.deltaspike.test.testcontrol.uc005.MockedJsfContainerTest;
import org.apache.deltaspike.test.testcontrol.uc006.SkipExternalContainerTest;
import org.apache.deltaspike.test.testcontrol.uc009.JsfContainerTest;
import org.apache.deltaspike.test.testcontrol.uc010.JsfContainerPerTestMethodTest;
import org.apache.deltaspike.testcontrol.impl.jsf.MockedJsf2TestContainer;
import org.apache.deltaspike.testcontrol.impl.jsf.MyFacesContainerAdapter;
import org.apache.deltaspike.testcontrol.impl.jsf.MyFacesContainerPerTestMethodAdapter;
import org.apache.deltaspike.testcontrol.spi.ExternalContainer;
import org.apache.deltaspike.testcontrol.spi.TestAware;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class InternalMyFacesTestContainerAdapter implements TestAware, ExternalContainer
{
    private ExternalContainer wrapped;

    @Override
    public void boot()
    {
        wrapped.boot();
    }

    @Override
    public void shutdown()
    {
        wrapped.shutdown();
    }

    @Override
    public int getOrdinal()
    {
        return wrapped.getOrdinal();
    }

    @Override
    public void startScope(Class<? extends Annotation> scopeClass)
    {
        wrapped.startScope(scopeClass);
    }

    @Override
    public void stopScope(Class<? extends Annotation> scopeClass)
    {
        wrapped.stopScope(scopeClass);
    }

    @Override
    public void setTestClass(Class testClass)
    {
        if (MockedJsfContainerTest.class.equals(testClass) ||
            SkipExternalContainerTest.class.equals(testClass))
        {
            this.wrapped = new MockedJsf2TestContainer();
        }
        else if (JsfContainerTest.class.equals(testClass))
        {
            this.wrapped = new MyFacesContainerAdapter();
            ((TestAware)this.wrapped).setTestClass(testClass);
        }
        else if (JsfContainerPerTestMethodTest.class.equals(testClass))
        {
            this.wrapped = new MyFacesContainerPerTestMethodAdapter();
            ((TestAware)this.wrapped).setTestClass(testClass);
        }
        else
        {
            this.wrapped = new ExternalContainer()
            {
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
                    return 0;
                }

                @Override
                public void startScope(Class<? extends Annotation> scopeClass)
                {
                }

                @Override
                public void stopScope(Class<? extends Annotation> scopeClass)
                {
                }
            };
        }
    }

    @Override
    public void setTestMethod(Method testMethod)
    {
        if (this.wrapped instanceof TestAware)
        {
            ((TestAware)this.wrapped).setTestMethod(testMethod);
        }
    }
}
