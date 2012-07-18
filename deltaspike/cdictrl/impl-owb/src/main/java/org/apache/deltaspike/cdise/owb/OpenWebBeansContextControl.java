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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;

import java.lang.annotation.Annotation;

import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.context.type.ContextTypes;

/**
 * OWB specific impl of the {@link ContextControl}
 */
@Dependent
@SuppressWarnings("UnusedDeclaration")
public class OpenWebBeansContextControl implements ContextControl
{
    @Override
    public void startContexts()
    {
        ContextFactory contextFactory = getContextFactory();

        Object mockSession = null;
        if (isServletApiAvailable())
        {
            mockSession = OwbHelper.getMockSession();
        }
        Object mockServletContextEvent = null;
        if (isServletApiAvailable())
        {
            mockServletContextEvent = OwbHelper.getMockServletContextEvent();
        }

        contextFactory.initSingletonContext(mockServletContextEvent);
        contextFactory.initApplicationContext(mockServletContextEvent);
        contextFactory.initSessionContext(mockSession);
        contextFactory.initRequestContext(null);
        contextFactory.initConversationContext(null);
    }

    public void stopContexts()
    {
        stopSessionScope();
        stopConversationScope();
        stopRequestScope();
        stopApplicationScope();
        stopSingletonScope();
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

    private static boolean isServletApiAvailable()
    {
        try
        {
            Class servletClass = Class.forName("javax.servlet.http.HttpSession");
            return servletClass != null;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }


    /*
    * start scopes
    */

    private void startApplicationScope()
    {
        ContextFactory contextFactory = getContextFactory();
        Object mockServletContextEvent = null;
        if (isServletApiAvailable())
        {
            mockServletContextEvent = OwbHelper.getMockServletContextEvent();
        }
        contextFactory.initApplicationContext(mockServletContextEvent);
    }

    private void startSessionScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Object mockSession = null;
        if (isServletApiAvailable())
        {
            mockSession = OwbHelper.getMockSession();
        }
        contextFactory.initSessionContext(mockSession);
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
        if (context != null)
        {
            Object mockServletContextEvent = null;
            if (isServletApiAvailable())
            {
                mockServletContextEvent = OwbHelper.getMockServletContextEvent();
            }
            contextFactory.destroySingletonContext(mockServletContextEvent);
        }
    }

    private void stopApplicationScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.APPLICATION);
        if (context != null)
        {
            Object mockServletContextEvent = null;
            if (isServletApiAvailable())
            {
                mockServletContextEvent = OwbHelper.getMockServletContextEvent();
            }
            contextFactory.destroyApplicationContext(mockServletContextEvent);
        }
    }

    private void stopSessionScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.SESSION);
        if (context != null)
        {
            Object mockSession = null;
            if (isServletApiAvailable())
            {
                mockSession = OwbHelper.getMockSession();
            }
            contextFactory.destroySessionContext(mockSession);
        }
    }

    private void stopRequestScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.REQUEST);
        if (context != null)
        {
            contextFactory.destroyRequestContext(null);
        }
    }

    private void stopConversationScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.CONVERSATION);
        if (context != null)
        {
            contextFactory.destroyConversationContext();
        }
    }

    private ContextFactory getContextFactory()
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        return webBeansContext.getContextFactory();
    }
}
