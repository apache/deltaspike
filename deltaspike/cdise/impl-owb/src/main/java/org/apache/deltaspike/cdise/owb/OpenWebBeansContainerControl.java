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
package org.apache.deltaspike.cdise.owb;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.context.type.ContextTypes;
import org.apache.webbeans.spi.ContainerLifecycle;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OpenWebBeans specific implementation of {@link org.apache.deltaspike.cdise.api.CdiContainer}.
 */
public class OpenWebBeansContainerControl implements CdiContainer
{
    private static final Logger LOG = Logger.getLogger(OpenWebBeansContainerControl.class.getName());

    private ContainerLifecycle lifecycle;
    private MockServletContext servletContext;
    private MockHttpSession session;

    private Boolean resetSuccessful;

    @Override
    public  BeanManager getBeanManager()
    {
        return lifecycle.getBeanManager();
    }

    @Override
    public void start()
    {
        bootContainer();
        startContexts();
    }

    @Override
    public void stop()
    {
        stopContexts();
        shutdownContainer();
    }

    @Override
    public void bootContainer()
    {
        servletContext = new MockServletContext();
        session = new MockHttpSession();

        lifecycle = WebBeansContext.getInstance().getService(ContainerLifecycle.class);
        lifecycle.startApplication(servletContext);
    }

    @Override
    public void shutdownContainer()
    {
        if (lifecycle != null) 
        {
            lifecycle.stopApplication(servletContext);
        }
    }

    @Override
    public void startContexts()
    {
        ContextFactory contextFactory = getContextFactory();

        contextFactory.initSingletonContext(servletContext);
        contextFactory.initApplicationContext(servletContext);
        contextFactory.initSessionContext(session);
        contextFactory.initRequestContext(null);
        contextFactory.initConversationContext(null);
    }

    @Override
    public void startContext(Class<? extends Annotation> scopeClass)
    {
        if (scopeClass.isAssignableFrom(ApplicationScoped.class))
        {
            startApplicationScope();
        }
        else if (scopeClass.isAssignableFrom(SessionScoped.class))
        {
            startSessionScope();
        }
        else if (scopeClass.isAssignableFrom(RequestScoped.class))
        {
            startRequestScope();
        }
        else if (scopeClass.isAssignableFrom(ConversationScoped.class))
        {
            startConversationScope();
        }
    }

    public void stopContexts()
    {
        stopSessionScope();
        stopConversationScope();
        stopRequestScope();
        stopApplicationScope();
        stopSingletonScope();
    }

    public void stopContext(Class<? extends Annotation> scopeClass)
    {
        if (scopeClass.isAssignableFrom(ApplicationScoped.class))
        {
            stopApplicationScope();
        }
        else if (scopeClass.isAssignableFrom(SessionScoped.class))
        {
            stopSessionScope();
        }
        else if (scopeClass.isAssignableFrom(RequestScoped.class))
        {
            stopRequestScope();
        }
        else if (scopeClass.isAssignableFrom(ConversationScoped.class))
        {
            stopConversationScope();
        }
    }

    /*
     * start scopes
     */

    private void startApplicationScope()
    {
        ContextFactory contextFactory = getContextFactory();

        contextFactory.initApplicationContext(servletContext);
    }

    private void startSessionScope()
    {
        ContextFactory contextFactory = getContextFactory();

        contextFactory.initSessionContext(session);
    }

    private void startRequestScope()
    {
        ContextFactory contextFactory = getContextFactory();

        contextFactory.initRequestContext(null);
    }

    private void startConversationScope()
    {
        ContextFactory contextFactory = getContextFactory();

        contextFactory.initConversationContext(null);
    }

    /*
     * stop scopes
     */

    private void stopSingletonScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.SINGLETON);
        if (context != null && context.isActive())
        {
            contextFactory.destroySingletonContext(servletContext);
            resetCache();
        }
        else
        {
            logDestroyOfInactiveContext(Singleton.class.getName());
        }
    }

    private void stopApplicationScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.APPLICATION);
        if (context != null && context.isActive())
        {
            contextFactory.destroyApplicationContext(servletContext);
            resetCache();
        }
        else
        {
            logDestroyOfInactiveContext(ApplicationScoped.class.getName());
        }
    }

    private void stopSessionScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.SESSION);
        if (context != null && context.isActive())
        {
            contextFactory.destroySessionContext(session);
            resetCache();
        }
        else
        {
            logDestroyOfInactiveContext(SessionScoped.class.getName());
        }
    }

    private void stopRequestScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.REQUEST);
        if (context != null && context.isActive())
        {
            contextFactory.destroyRequestContext(null);
            resetCache();
        }
        else
        {
            logDestroyOfInactiveContext(RequestScoped.class.getName());
        }
    }

    private void stopConversationScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.CONVERSATION);
        if (context != null && context.isActive())
        {
            contextFactory.destroyConversationContext();
            resetCache();
        }
        else
        {
            logDestroyOfInactiveContext(ConversationScoped.class.getName());
        }
    }

    //workaround for OWB-650
    private void resetCache()
    {
        if (Boolean.FALSE.equals(this.resetSuccessful))
        {
            return;
        }

        BeanManager beanManager = getBeanManager();

        try
        {
            Field cacheProxiesField = beanManager.getClass().getDeclaredField("cacheProxies");
            cacheProxiesField.setAccessible(true);
            Map cacheProxies = (Map)cacheProxiesField.get(beanManager);

            if (cacheProxies != null)
            {
                cacheProxies.clear();
                this.resetSuccessful = true;
            }
        }
        catch (Exception e)
        {
            //do nothing - it's a different version of OWB which isn't tested but
            //might not have a cache and is therefore compatible.
            this.resetSuccessful = false;
        }
        catch (LinkageError e)
        {
            //do nothing - a new version of owb is used which introduced other required dependencies.
            //OWB-650 should be fixed in this version already
            this.resetSuccessful = false;
        }
    }

    private ContextFactory getContextFactory()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        return webBeansContext.getContextFactory();
    }

    private void logDestroyOfInactiveContext(String contextName)
    {
        LOG.log(Level.WARNING,
                "destroy was called for an inactive context (" + contextName + ")");
    }
}
