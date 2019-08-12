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
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;

import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationHandler;
import java.lang.reflect.Constructor;

public class NavigationHandlerAwareApplication extends ApplicationWrapper implements Deactivatable
{
    private static volatile Boolean manualNavigationHandlerWrapperMode;
    private static Class navigationHandlerWrapperClass;
    private final Application wrapped;

    public NavigationHandlerAwareApplication(Application wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public NavigationHandler getNavigationHandler()
    {
        return wrapNavigationHandler(this.wrapped.getNavigationHandler());
    }

    private NavigationHandler wrapNavigationHandler(NavigationHandler handler)
    {
        NavigationHandler result = null;

        if (manualNavigationHandlerWrapperMode == null)
        {
            lazyInit();
        }

        //jsf 2.2+
        if (!manualNavigationHandlerWrapperMode)
        {
            result = wrapNavigationHandlerWithNewWrapper(handler);
        }
        if (result != null)
        {
            return result;
        }

        //jsf 2.0 and 2.1
        return new DeltaSpikeNavigationHandler(handler);
    }

    private static synchronized void lazyInit()
    {
        if (manualNavigationHandlerWrapperMode != null)
        {
            return;
        }

        Class wrapperClass = ClassUtils
            .tryToLoadClassForName("javax.faces.application.ConfigurableNavigationHandlerWrapper");

        if (wrapperClass != null)
        {
            navigationHandlerWrapperClass =
                ClassUtils.tryToLoadClassForName(
                    "org.apache.deltaspike.jsf.impl.navigation.DeltaSpikeNavigationHandlerWrapper");

            if (navigationHandlerWrapperClass != null)
            {
                manualNavigationHandlerWrapperMode = false;
            }
            else
            {
                JsfUtils.logWrongModuleUsage(NavigationHandlerAwareApplication.class.getName());
                manualNavigationHandlerWrapperMode = true;
            }
        }
        else
        {
            manualNavigationHandlerWrapperMode = true;
        }
    }

    private NavigationHandler wrapNavigationHandlerWithNewWrapper(NavigationHandler handler)
    {
        if (ConfigurableNavigationHandler.class.isAssignableFrom(handler.getClass()))
        {
            try
            {
                Constructor deltaSpikeNavigationHandlerWrapperConstructor =
                    navigationHandlerWrapperClass.getConstructor(ConfigurableNavigationHandler.class);

                NavigationHandler navigationHandlerWrapper =
                    (NavigationHandler)deltaSpikeNavigationHandlerWrapperConstructor.newInstance(handler);
                return  navigationHandlerWrapper;
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }

        return null;
    }

    @Override
    public Application getWrapped()
    {
        return wrapped;
    }
}
