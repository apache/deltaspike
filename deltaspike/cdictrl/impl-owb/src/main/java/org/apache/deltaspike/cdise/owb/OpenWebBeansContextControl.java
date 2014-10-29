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
import javax.inject.Singleton;

import java.lang.annotation.Annotation;

import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;

/**
 * OWB specific impl of the {@link ContextControl}
 */
@Dependent
@SuppressWarnings("UnusedDeclaration")
public class OpenWebBeansContextControl implements ContextControl
{

    /**
     * we cannot directly link to MockHttpSession as this would lead to
     * NoClassDefFound errors for cases where no servlet-api is on the classpath.
     * E.g in pure SE environments.
     */
    private static ThreadLocal<Object> mockSessions = new ThreadLocal<Object>();


    @Override
    public void startContexts()
    {
        ContextsService contextsService = getContextsService();

        startSingletonScope();
        startApplicationScope();
        startSessionScope();
        startRequestScope();
        startConversationScope();
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

    static boolean isServletApiAvailable()
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

    private void startSingletonScope()
    {
        ContextsService contextsService = getContextsService();
        Object mockServletContext = null;
        if (isServletApiAvailable())
        {
            mockServletContext = OwbHelper.getMockServletContext();
        }
        contextsService.startContext(Singleton.class, mockServletContext);
    }

    private void startApplicationScope()
    {
        ContextsService contextsService = getContextsService();
        Object mockServletContext = null;
        if (isServletApiAvailable())
        {
            mockServletContext = OwbHelper.getMockServletContext();
        }
        contextsService.startContext(ApplicationScoped.class, mockServletContext);
    }

    private void startSessionScope()
    {
        ContextsService contextsService = getContextsService();

        Object mockSession = null;
        if (isServletApiAvailable())
        {
            mockSession = mockSessions.get();
            if (mockSession == null)
            {
                // we simply use the ThreadName as 'sessionId'
                mockSession = OwbHelper.getMockSession(Thread.currentThread().getName());
                mockSessions.set(mockSession);
            }
        }
        contextsService.startContext(SessionScoped.class, mockSession);
    }

    private void startRequestScope()
    {
        ContextsService contextsService = getContextsService();

        contextsService.startContext(RequestScoped.class, null);
    }

    private void startConversationScope()
    {
        ContextsService contextsService = getContextsService();

        contextsService.startContext(ConversationScoped.class, null);
    }

    /*
     * stop scopes
     */

    private void stopSingletonScope()
    {
        ContextsService contextsService = getContextsService();

        Object mockServletContext = null;
        if (isServletApiAvailable())
        {
            mockServletContext = OwbHelper.getMockServletContext();
        }
        contextsService.endContext(Singleton.class, mockServletContext);
    }

    private void stopApplicationScope()
    {
        ContextsService contextsService = getContextsService();

        Object mockServletContext = null;
        if (isServletApiAvailable())
        {
            mockServletContext = OwbHelper.getMockServletContext();
        }
        contextsService.endContext(ApplicationScoped.class, mockServletContext);
    }

    private void stopSessionScope()
    {
        ContextsService contextsService = getContextsService();

        Object mockSession = null;
        if (isServletApiAvailable())
        {
            mockSession = mockSessions.get();
            mockSessions.set(null);
            mockSessions.remove();
        }
        contextsService.endContext(SessionScoped.class, mockSession);
    }

    private void stopRequestScope()
    {
        ContextsService contextsService = getContextsService();

        contextsService.endContext(RequestScoped.class, null);
    }

    private void stopConversationScope()
    {
        ContextsService contextsService = getContextsService();

        contextsService.endContext(ConversationScoped.class, null);
    }

    private ContextsService getContextsService()
    {
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        return webBeansContext.getContextsService();
    }


}
