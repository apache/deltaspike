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

import org.jboss.weld.context.AbstractSharedContext;
import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.context.bound.MutableBoundRequest;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Weld specific controller for all supported context implementations
 */
@Typed()
public class ContextController
{
    private static ThreadLocal<RequestContextHolder> requestContexts = new ThreadLocal<RequestContextHolder>();

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private BoundSessionContext sessionContext;

    @Inject
    private Instance<BoundRequestContext> requestContextFactory;

    @Inject
    private BoundConversationContext conversationContext;
    private Map<String, Object> sessionMap;

    private AtomicInteger sessionRefCounter = new AtomicInteger(0);

    private boolean singletonScopeStarted;

    void startApplicationScope()
    {
        // Welds ApplicationContext is always active
    }

    void stopApplicationScope()
    {
        if (applicationContext.isActive())
        {
            applicationContext.invalidate();

            //needed for weld < v1.1.9
            if (applicationContext instanceof AbstractSharedContext)
            {
                ((AbstractSharedContext) applicationContext).getBeanStore().clear();
            }
        }
    }

    //X TODO check if we can remove it
    void startSingletonScope()
    {
        if (singletonScopeStarted)
        {
            throw new IllegalStateException(Singleton.class.getName() + " started already");
        }
        singletonScopeStarted = true;
    }

    void stopSingletonScope()
    {
        singletonScopeStarted = false;
    }

    synchronized void startSessionScope()
    {
        if (sessionMap == null)
        {
            sessionMap = new HashMap<String, Object>();
        }

        sessionRefCounter.incrementAndGet();
        sessionContext.associate(sessionMap);
        sessionContext.activate();
    }

    synchronized void stopSessionScope()
    {
        if (sessionContext.isActive())
        {
            sessionContext.invalidate();
            sessionContext.deactivate();
            sessionContext.dissociate(sessionMap);
            if (sessionRefCounter.decrementAndGet() <= 0)
            {
                sessionMap = null;
            }
        }
    }

    synchronized void startConversationScope(String cid)
    {
        RequestContextHolder rcHolder = requestContexts.get();
        if (rcHolder == null)
        {
            startRequestScope();
            rcHolder = requestContexts.get();
        }
        conversationContext.associate(new MutableBoundRequest(rcHolder.requestMap, sessionMap));
        conversationContext.activate(cid);
    }

    synchronized void stopConversationScope()
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
            conversationContext.dissociate(new MutableBoundRequest(rcHolder.getRequestMap(), sessionMap));
        }
    }

    synchronized void startRequestScope()
    {
        RequestContextHolder rcHolder = requestContexts.get();
        if (rcHolder == null)
        {
            rcHolder = new RequestContextHolder(requestContextFactory.get(), new HashMap<String, Object>());
            requestContexts.set(rcHolder);
        }
        else
        {
            throw new IllegalStateException(RequestScoped.class.getName() + " started already");
        }

        rcHolder.getBoundRequestContext().associate(rcHolder.getRequestMap());
        rcHolder.getBoundRequestContext().activate();
    }

    synchronized void stopRequestScope()
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
