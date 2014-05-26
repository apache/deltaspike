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

import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;

import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationHandler;
import java.lang.reflect.Constructor;

public class NavigationHandlerAwareApplication extends ApplicationWrapper
{
    private final Application wrapped;

    public NavigationHandlerAwareApplication(Application wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public void setNavigationHandler(NavigationHandler handler)
    {
        Class wrapperClass = ClassUtils
            .tryToLoadClassForName("javax.faces.application.ConfigurableNavigationHandlerWrapper");

        //jsf 2.2+
        if (wrapperClass != null)
        {
            if (ConfigurableNavigationHandler.class.isAssignableFrom(handler.getClass()))
            {
                try
                {
                    Class deltaSpikeWrapperClass = ClassUtils.tryToLoadClassForName(
                        "org.apache.deltaspike.jsf.impl.navigation.DeltaSpikeNavigationHandlerWrapper");
                    Constructor deltaSpikeNavigationHandlerWrapperConstructor =
                        deltaSpikeWrapperClass.getConstructor(ConfigurableNavigationHandler.class);

                    NavigationHandler navigationHandlerWrapper =
                            (NavigationHandler)deltaSpikeNavigationHandlerWrapperConstructor.newInstance(handler);
                    this.wrapped.setNavigationHandler(navigationHandlerWrapper);
                    return;
                }
                catch (Exception e)
                {
                    throw ExceptionUtils.throwAsRuntimeException(e);
                }
            }
        }

        //jsf 2.0 and 2.1
        this.wrapped.setNavigationHandler(new DeltaSpikeNavigationHandler(handler));
    }

    @Override
    public Application getWrapped()
    {
        return wrapped;
    }
}
