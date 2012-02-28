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

import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.context.bound.MutableBoundRequest;

import javax.enterprise.context.ApplicationScoped;
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

    private boolean applicationScopeStarted;

    private boolean singletonScopeStarted;

    //X TODO check if we can remove it
    void startApplicationScope()
    {
        if (this.applicationScopeStarted)
        {
            throw new IllegalStateException(ApplicationScoped.class.getName() + " started already");
        }
        this.applicationScopeStarted = true;
    }

    void stopApplicationScope()
    {
        if (applicationContext.isActive())
        {
            applicationContext.invalidate();
            this.applicationScopeStarted = false;
        }
    }

    //X TODO check if we can remove it
    void startSingletonScope()
    {
        if (this.singletonScopeStarted)
        {
            throw new IllegalStateException(Singleton.class.getName() + " started already");
        }
        this.singletonScopeStarted = true;
    }

    void stopSingletonScope()
    {
        this.singletonScopeStarted = false;
    }

    void startSessionScope()
    {
        if (this.sessionMap == null)
        {
            this.sessionMap = new HashMap<String, Object>();
        }
        else
        {
            throw new IllegalStateException(SessionScoped.class.getName() + " started already");
        }

        this.sessionContext.associate(this.sessionMap);
        this.sessionContext.activate();
    }

    void stopSessionScope()
    {
        if (this.sessionContext.isActive())
        {
            try
            {
                this.sessionContext.invalidate();
                this.sessionContext.deactivate();
            }
            finally
            {
                this.sessionContext.dissociate(this.sessionMap);
            }
        }
    }

    void startConversationScope(String cid)
    {
        this.conversationContext.associate(new MutableBoundRequest(this.requestMap, this.sessionMap));
        this.conversationContext.activate(cid);
    }

    void stopConversationScope()
    {
        if (conversationContext.isActive())
        {
            try
            {
                this.conversationContext.invalidate();
                this.conversationContext.deactivate();
            }
            finally
            {
                this.conversationContext.dissociate(new MutableBoundRequest(this.requestMap, this.sessionMap));
            }
        }
    }

    void startRequestScope()
    {
        if (this.requestMap == null)
        {
            this.requestMap = new HashMap<String, Object>();
        }
        else
        {
            throw new IllegalStateException(RequestScoped.class.getName() + " started already");
        }

        this.requestContext.associate(this.requestMap);
        this.requestContext.activate();
    }

    void stopRequestScope()
    {
        if (this.requestContext.isActive())
        {
            try
            {
                this.requestContext.invalidate();
                this.requestContext.deactivate();
            }
            finally
            {
                this.requestContext.dissociate(this.requestMap);
            }
        }
    }
}
