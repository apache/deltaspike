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
package org.apache.deltaspike.cdise.weld;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.apache.deltaspike.cdise.api.ContextControl;
import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.context.bound.MutableBoundRequest;

/**
 * Weld specific impl of the {@link org.apache.deltaspike.cdise.api.ContextControl}
 */
@Dependent
@SuppressWarnings("UnusedDeclaration")
public class WeldContextControl implements ContextControl
{
    private static ThreadLocal<RequestContextHolder> requestContexts = new ThreadLocal<RequestContextHolder>();
    private static ThreadLocal<Map<String, Object>> sessionMaps = new ThreadLocal<Map<String, Object>>();



    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private BoundSessionContext sessionContext;

    @Inject
    private Instance<BoundRequestContext> requestContextFactory;

    @Inject
    private BoundConversationContext conversationContext;



    @Override
    public void startContexts()
    {
        startApplicationScope();
        startSessionScope();
        startRequestScope();
        startConversationScope(null);
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
            startConversationScope(null);
        }
    }

    /**
     * Stops Conversation, Request and Session contexts.
     * Does NOT stop Application context, only invalidates 
     * App scoped beans, as in Weld this context always active and clears
     * automatically on shutdown.
     *
     * {@inheritDoc}
     */
    @Override
    public void stopContexts()
    {
        stopConversationScope();
        stopRequestScope();
        stopSessionScope();
        stopApplicationScope();
    }

    @Override
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
     * This is a no-op method. In Weld Application Context is active as soon as the container starts
     */
    private void startApplicationScope()
    {
        // No-op, in Weld Application context is always active
    }

    /**
     * Weld Application context is active from container start to its shutdown
     * This method merely clears out all ApplicationScoped beans BUT the context
     * will still be active which may result in immediate re-creation of some beans.
     */
    private void stopApplicationScope()
    {
        // Welds ApplicationContext gets cleaned at shutdown.
        // Weld App context should be always active
        if (applicationContext.isActive())
        {
            // destroys the bean instances, but the context stays active
            applicationContext.invalidate();
        }
    }

    void startRequestScope()
    {
        RequestContextHolder rcHolder = requestContexts.get();
        if (rcHolder == null)
        {
            rcHolder = new RequestContextHolder(requestContextFactory.get(), new HashMap<String, Object>());
            requestContexts.set(rcHolder);
            rcHolder.getBoundRequestContext().associate(rcHolder.getRequestMap());
            rcHolder.getBoundRequestContext().activate();
        }
    }

    void stopRequestScope()
    {
        RequestContextHolder rcHolder = requestContexts.get();
        if (rcHolder != null && rcHolder.getBoundRequestContext().isActive())
        {
            rcHolder.getBoundRequestContext().invalidate();
            rcHolder.getBoundRequestContext().deactivate();
            rcHolder.getBoundRequestContext().dissociate(rcHolder.getRequestMap());
            requestContexts.set(null);
            requestContexts.remove();
        }
    }

    private void startSessionScope()
    {
        Map<String, Object> sessionMap = sessionMaps.get();
        if (sessionMap == null)
        {
            sessionMap = new HashMap<String, Object>();
            sessionMaps.set(sessionMap);
        }

        sessionContext.associate(sessionMap);
        sessionContext.activate();

    }

    private void stopSessionScope()
    {
        if (sessionContext.isActive())
        {
            sessionContext.invalidate();
            sessionContext.deactivate();
            sessionContext.dissociate(sessionMaps.get());

            sessionMaps.set(null);
            sessionMaps.remove();
        }
    }

    void startConversationScope(String cid)
    {
        RequestContextHolder rcHolder = requestContexts.get();
        if (rcHolder == null)
        {
            startRequestScope();
            rcHolder = requestContexts.get();
        }
        conversationContext.associate(new MutableBoundRequest(rcHolder.requestMap, sessionMaps.get()));
        conversationContext.activate(cid);
    }

    void stopConversationScope()
    {
        RequestContextHolder rcHolder = requestContexts.get();
        if (rcHolder == null)
        {
            startRequestScope();
            rcHolder = requestContexts.get();
        }
        if (conversationContext.isActive())
        {
            conversationContext.invalidate();
            conversationContext.deactivate();
            conversationContext.dissociate(new MutableBoundRequest(rcHolder.getRequestMap(), sessionMaps.get()));
        }
    }


    private static class RequestContextHolder
    {
        private final BoundRequestContext boundRequestContext;
        private final Map<String, Object> requestMap;

        private RequestContextHolder(BoundRequestContext boundRequestContext, Map<String, Object> requestMap)
        {
            this.boundRequestContext = boundRequestContext;
            this.requestMap = requestMap;
        }

        public BoundRequestContext getBoundRequestContext()
        {
            return boundRequestContext;
        }

        public Map<String, Object> getRequestMap()
        {
            return requestMap;
        }
    }

}
