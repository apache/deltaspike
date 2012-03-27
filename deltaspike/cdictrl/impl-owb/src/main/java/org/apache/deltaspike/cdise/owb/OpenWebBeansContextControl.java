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
import java.util.logging.Logger;

import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.context.type.ContextTypes;

/**
 * OWB specific impl of the {@link ContextControl}
 */
@Dependent
public class OpenWebBeansContextControl implements ContextControl
{

    private static final Logger LOG = Logger.getLogger(OpenWebBeansContextControl.class.getName());

    private Boolean servletApiAvailable = null;
    
    private Object session = null;
    private Object servletContext = null;
    
    protected boolean isServletApiAvailable()
    {
        if (servletApiAvailable == null)
        {
            try
            {
                Class servletClass = Class.forName("javax.servlet.http.HttpSession");
                servletApiAvailable = servletClass != null;
                LOG.fine("Servlet API available: " + servletApiAvailable);
            }
            catch (ClassNotFoundException e)
            {
                servletApiAvailable = Boolean.FALSE;
            }
        }
        return servletApiAvailable;
    }

    protected Object getMockSession()
    {
        if (isServletApiAvailable() && session == null)
        {
            session = new MockHttpSession();
        }

        return session;
    }
    
    protected Object getMockServletContext()
    {
        if (isServletApiAvailable() && servletContext == null)
        {
            servletContext  = new MockServletContext();
        }

        return servletContext;
    }

    @Override
    public void startContexts()
    {
        ContextFactory contextFactory = getContextFactory();

        contextFactory.initSingletonContext(getMockServletContext());
        contextFactory.initApplicationContext(getMockServletContext());
        contextFactory.initSessionContext(getMockSession());
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

    /*
    * start scopes
    */

    private void startApplicationScope()
    {
        ContextFactory contextFactory = getContextFactory();

        contextFactory.initApplicationContext(getMockServletContext());
    }

    private void startSessionScope()
    {
        ContextFactory contextFactory = getContextFactory();

        contextFactory.initSessionContext(getMockSession());
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
            contextFactory.destroySingletonContext(getMockServletContext());
        }
    }

    private void stopApplicationScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.APPLICATION);
        if (context != null)
        {
            contextFactory.destroyApplicationContext(getMockServletContext());
        }
    }

    private void stopSessionScope()
    {
        ContextFactory contextFactory = getContextFactory();

        Context context = contextFactory.getStandardContext(ContextTypes.SESSION);
        if (context != null)
        {
            contextFactory.destroySessionContext(getMockSession());
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
