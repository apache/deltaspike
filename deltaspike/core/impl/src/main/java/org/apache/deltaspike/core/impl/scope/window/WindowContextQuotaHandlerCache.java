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

import org.apache.deltaspike.core.spi.scope.window.WindowContext;

import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.io.Serializable;

@RequestScoped
public class WindowContextQuotaHandlerCache implements Serializable
{
    private String checkedWindowId;
    private String windowIdToRemove;

    @Inject
    private WindowContext windowContext;

    /**
     * @param currentWindowId window-id which gets processed right now
     * @return true if the previously checked window-id is the same, false otherwise
     */
    public boolean cacheWindowId(String currentWindowId)
    {
        boolean result = currentWindowId.equals(this.checkedWindowId);
        this.checkedWindowId = currentWindowId;
        return result;
    }

    public void setWindowIdToDestroy(String windowIdToRemove)
    {
        this.windowIdToRemove = windowIdToRemove;
    }

    @PreDestroy
    public void cleanup()
    {
        if (this.windowIdToRemove != null)
        {
            this.windowContext.closeWindow(this.windowIdToRemove);
        }
    }
}
