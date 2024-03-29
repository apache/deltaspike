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
import org.apache.myfaces.test.mock.MockedJsfTestContainer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import java.lang.annotation.Annotation;

/**
 * Optional adapter for MockedJsfTestContainer
 * Requires MyFaces-Test v1.0.6 or higher
 */
@ApplicationScoped
public class MockedJsfTestContainerAdapter implements ExternalContainer
{
    private final MockedJsfTestContainer mockedMyFacesTestContainer = new MockedJsfTestContainer();

    public void boot()
    {
        this.mockedMyFacesTestContainer.setUp();
    }

    @Override
    public void startScope(Class<? extends Annotation> scopeClass)
    {
        if (RequestScoped.class.equals(scopeClass))
        {
            this.mockedMyFacesTestContainer.startRequest();
        }
        else if (SessionScoped.class.equals(scopeClass))
        {
            this.mockedMyFacesTestContainer.startSession();
        }
    }

    @Override
    public void stopScope(Class<? extends Annotation> scopeClass)
    {
        if (RequestScoped.class.equals(scopeClass))
        {
            this.mockedMyFacesTestContainer.endRequest();
        }
        else if (SessionScoped.class.equals(scopeClass))
        {
            this.mockedMyFacesTestContainer.endSession();
        }
    }

    public void shutdown()
    {
        this.mockedMyFacesTestContainer.tearDown();
    }

    @Override
    public int getOrdinal()
    {
        return 1000; //default in ds
    }
}
