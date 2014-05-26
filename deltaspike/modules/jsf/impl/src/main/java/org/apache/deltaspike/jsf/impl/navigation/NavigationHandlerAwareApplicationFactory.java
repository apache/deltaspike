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
package org.apache.deltaspike.jsf.impl.navigation;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;

public class NavigationHandlerAwareApplicationFactory extends ApplicationFactory implements Deactivatable
{
    private final ApplicationFactory wrapped;

    public NavigationHandlerAwareApplicationFactory(ApplicationFactory wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public Application getApplication()
    {
        if (ClassDeactivationUtils.isActivated(getClass()))
        {
            return new NavigationHandlerAwareApplication(wrapped.getApplication());
        }

        return wrapped.getApplication();
    }

    @Override
    public void setApplication(Application application)
    {
        wrapped.setApplication(application);
    }

    public ApplicationFactory getWrapped()
    {
        return wrapped;
    }
}
