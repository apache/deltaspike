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
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.context.beanstore.BeanStore;
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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
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

    private Boolean resetSuccessful;

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
        if (Boolean.FALSE.equals(this.resetSuccessful) /*|| TODO detect weld 2.x+*/)
        {
            if (applicationContext.isActive())
            {
                applicationContext.invalidate();
                this.applicationScopeStarted = false;
            }
            return;
        }

        if (applicationContext.isActive())
        {
            //workaround for weld 1.x (see WELD-1072)
            org.jboss.weld.bootstrap.api.Singleton<BeanStore> beanStoreHolder = null;
            BeanStore originalBeanStore = null;
            try
            {
                Field field = this.applicationContext.getClass().getSuperclass().getDeclaredField("beanStore");
                field.setAccessible(true);
                beanStoreHolder = (org.jboss.weld.bootstrap.api.Singleton)field.get(this.applicationContext);
                final BeanStore beanStore = beanStoreHolder.get();
                originalBeanStore = beanStore;

                beanStoreHolder.set(new BeanStore()
                {
                    @Override
                    public <T> ContextualInstance<T> get(String id)
                    {
                        return beanStore.get(id);
                    }

                    @Override
                    public boolean contains(String id)
                    {
                        return beanStore.contains(id);
                    }

                    @Override
                    public void clear()
                    {
                        //do nothing
                    }

                    @Override
                    public Iterator<String> iterator()
                    {
                        return beanStore.iterator();
                    }

                    @Override
                    public <T> void put(String id, ContextualInstance<T> contextualInstance)
                    {
                        beanStore.put(id, contextualInstance);
                    }
                });
            }
            catch (Exception e)
            {
                //do nothing
                this.resetSuccessful = false;
            }
            catch (LinkageError e)
            {
                //do nothing - a new version of weld is used which introduced other required dependencies.
                //WELD-1072 should be fixed in this version already
                this.resetSuccessful = false;
            }

            applicationContext.invalidate();

            if (beanStoreHolder != null)
            {
                Iterator<String> idIterator = originalBeanStore.iterator();

                String currentId;
                ContextualInstance<Object> currentContextualInstance;
                while (idIterator.hasNext())
                {
                    currentId = idIterator.next();
                    currentContextualInstance = originalBeanStore.get(currentId);

                    //keep (weld) internal application scoped beans - TODO check possible side-effects
                    if (currentContextualInstance.getInstance().getClass().getName().startsWith("org.jboss."))
                    {
                        //internalBeanList.add(currentContextualInstance);
                        continue;
                    }
                    idIterator.remove();
                }

                beanStoreHolder.set(originalBeanStore);
            }

            this.applicationScopeStarted = false;

            this.resetSuccessful = true;
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
            this.sessionContext.invalidate();
            this.sessionContext.deactivate();
            this.sessionContext.dissociate(this.sessionMap);
            this.sessionMap = null;
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
            this.conversationContext.invalidate();
            this.conversationContext.deactivate();
            this.conversationContext.dissociate(new MutableBoundRequest(this.requestMap, this.sessionMap));
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
            this.requestContext.invalidate();
            this.requestContext.deactivate();
            this.requestContext.dissociate(this.requestMap);
            this.requestMap = null;
        }
    }
}
