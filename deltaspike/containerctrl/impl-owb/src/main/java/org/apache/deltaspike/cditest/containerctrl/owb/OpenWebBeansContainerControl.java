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
package org.apache.deltaspike.cditest.containerctrl.owb;

import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Singleton;

import org.apache.deltaspike.containerctrl.api.ContainerControl;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.context.type.ContextTypes;
import org.apache.webbeans.spi.ContainerLifecycle;

/**
 * OpenWebBeans specific implementation of {@link ContainerControl}.
 */
public class OpenWebBeansContainerControl implements ContainerControl
{
    private static final Logger logger = Logger.getLogger(OpenWebBeansContainerControl.class.getName());

    private ContainerLifecycle  lifecycle = null;
    private MockServletContext servletContext = null;
    private MockHttpSession     session = null;

    public void bootContainer() throws Exception 
    {
        servletContext = new MockServletContext();
        session = new MockHttpSession();
        lifecycle = WebBeansContext.getInstance().getService(ContainerLifecycle.class);
        lifecycle.startApplication(servletContext);
    }

    public void shutdownContainer() throws Exception 
    {
        if (lifecycle != null) 
        {
            lifecycle.stopApplication(servletContext);
        }
    }

    public void startContexts()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        ContextFactory contextFactory = webBeansContext.getContextFactory();

        contextFactory.initSingletonContext(servletContext);
        contextFactory.initApplicationContext(servletContext);
        contextFactory.initSessionContext(session);
        contextFactory.initConversationContext(null);
        contextFactory.initRequestContext(null);
    }

    private void startApplicationScope()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        ContextFactory contextFactory = webBeansContext.getContextFactory();

        contextFactory.initApplicationContext(servletContext);
    }

    public void startConversationScope()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        ContextFactory contextFactory = webBeansContext.getContextFactory();

        contextFactory.initConversationContext(null);
    }

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

    private void startRequestScope()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        ContextFactory contextFactory = webBeansContext.getContextFactory();

        contextFactory.initRequestContext(null);
    }

    private void startSessionScope()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        ContextFactory contextFactory = webBeansContext.getContextFactory();

        contextFactory.initSessionContext(session);
    }

    public void stopContexts()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        ContextFactory contextFactory = webBeansContext.getContextFactory();

        stopSessionScope();
        stopConversationScope();
        stopRequestScope();
        stopApplicationScope();

        Context context = contextFactory.getStandardContext(ContextTypes.SINGLETON);
        if (context != null && context.isActive())
        {
            contextFactory.destroySingletonContext(servletContext);
        }
        else
        {
            logger.log(Level.WARNING,
                       "destroy was called for an inactive context (" + Singleton.class.getName() + ")");
        }

    }

    public void stopApplicationScope()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        ContextFactory contextFactory = webBeansContext.getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.APPLICATION);
        if (context != null && context.isActive())
        {
            contextFactory.destroyApplicationContext(servletContext);
        }
        else
        {
            logger.log(Level.WARNING,
                       "destroy was called for an inactive context (" + ApplicationScoped.class.getName() + ")");
        }
    }

    public void stopConversationScope()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        ContextFactory contextFactory = webBeansContext.getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.CONVERSATION);
        if (context != null && context.isActive())
        {
            contextFactory.destroyConversationContext();
        }
        else
        {
            logger.log(Level.WARNING,
                       "destroy was called for an inactive context (" + ConversationScoped.class.getName() + ")");
        }
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

    public void stopRequestScope()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        ContextFactory contextFactory = webBeansContext.getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.REQUEST);
        if (context != null && context.isActive())
        {
            contextFactory.destroyRequestContext(null);
        }
        else
        {
            logger.log(Level.WARNING,
                       "destroy was called for an inactive context (" + RequestScoped.class.getName() + ")");
        }
    }

    public void stopSessionScope()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        ContextFactory contextFactory = webBeansContext.getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.SESSION);
        if (context != null && context.isActive())
        {
            contextFactory.destroySessionContext(session);
        }
        else
        {
            logger.log(Level.WARNING,
                       "destroy was called for an inactive context (" + SessionScoped.class.getName() + ")");
        }
    }
    
    public  BeanManager getBeanManager() 
    {
        return lifecycle.getBeanManager();
    }

}
