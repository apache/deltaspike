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
package org.apache.deltaspike.core.impl.scope.window;

import org.apache.deltaspike.core.api.config.base.CoreBaseConfig;
import org.apache.deltaspike.core.spi.scope.window.WindowContextQuotaHandler;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.util.Stack;

@SessionScoped
//could be also dependent-scoped since we only inject it in one session-scoped bean, however,
//if users would like to customize the behavior they wouldn't be able to use it (if it would be dependent-scoped)
public class DefaultWindowContextQuotaHandler implements WindowContextQuotaHandler
{
    protected int maxWindowContextCount;

    @Inject
    private WindowContextQuotaHandlerCache quotaHandlerCache;

    private Stack<String> windowIdStack = new Stack<String>();

    @PostConstruct
    protected void init()
    {
        this.maxWindowContextCount = CoreBaseConfig.ScopeCustomization.WindowRestriction.MAX_COUNT;
    }

    public synchronized /*no issue due to session-scoped instance*/ void checkWindowContextQuota(String windowId)
    {
        if (windowId == null)
        {
            return;
        }

        if (this.quotaHandlerCache.cacheWindowId(windowId))
        {
            return;
        }

        /*
         * the following part gets executed only once per request, if the window-id is the same
         */

        if (this.windowIdStack.contains(windowId))
        {
            if (windowIdStack.size() > 1) //don't move it up if there is just one entry
            {
                this.windowIdStack.remove(windowId);
                this.windowIdStack.push(windowId);
            }
        }
        else
        {
            this.windowIdStack.push(windowId);
            if (this.windowIdStack.size() > this.maxWindowContextCount)
            {
                String windowIdToRemove = this.windowIdStack.remove(0);
                //destroy it lazily at the end of the request to avoid an overhead during the request
                //which might be caused by pre-destroy logic of window-scoped beans
                this.quotaHandlerCache.setWindowIdToDestroy(windowIdToRemove);
            }
        }
    }
}
