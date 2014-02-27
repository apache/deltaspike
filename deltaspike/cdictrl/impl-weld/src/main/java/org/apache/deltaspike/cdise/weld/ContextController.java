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
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Weld specific controller for all supported context implementations
 */
@Typed()
public class ContextController
{
    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private BoundSessionContext sessionContext;

    @Inject
    private BoundRequestContext requestContext;

    @Inject
    private BoundConversationContext conversationContext;

    private Map<String, Object> sessionMap;

    private Map<String, Object> requestMap;

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

    void startSessionScope()
    {
        if (sessionMap == null)
        {
            sessionMap = new HashMap<String, Object>();
        }
        else
        {
            throw new IllegalStateException(SessionScoped.class.getName() + " started already");
        }

        sessionContext.associate(sessionMap);
        sessionContext.activate();
    }

    void stopSessionScope()
    {
        if (sessionContext.isActive())
        {
            sessionContext.invalidate();
            sessionContext.deactivate();
            sessionContext.dissociate(sessionMap);
            sessionMap = null;
        }
    }

    void startConversationScope(String cid)
    {
        conversationContext.associate(new MutableBoundRequest(requestMap, sessionMap));
        conversationContext.activate(cid);
    }

    void stopConversationScope()
    {
        if (conversationContext.isActive())
        {
            conversationContext.invalidate();
            conversationContext.deactivate();
            conversationContext.dissociate(new MutableBoundRequest(requestMap, sessionMap));
        }
    }

    void startRequestScope()
    {
        if (requestMap == null)
        {
            requestMap = new HashMap<String, Object>();
        }
        else
        {
            throw new IllegalStateException(RequestScoped.class.getName() + " started already");
        }

        requestContext.associate(requestMap);
        requestContext.activate();
    }

    void stopRequestScope()
    {
        if (requestContext.isActive())
        {
            requestContext.invalidate();
            requestContext.deactivate();
            requestContext.dissociate(requestMap);
            requestMap = null;
        }
    }
}
