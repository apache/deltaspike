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

import jakarta.enterprise.context.RequestScoped;

/**
 * Simple class which just provides a &#064;RequestScoped windowId.
 * This assures that there is maximum one single windowId associated
 * with a single Thread or Request. We use &#064;RequestScoped because
 * this also works in async-supported Servlets without having to
 * take care about moving info between ThreadLocals.
 */
@RequestScoped
public class WindowIdHolder
{
    private String windowId;

    /**
     * @return the detected windowId or <code>null</code> if not yet set.
     */
    public String getWindowId()
    {
        return windowId;
    }

    /**
     * Set the windowId for the current thread.
     * @param windowId
     */
    public void setWindowId(String windowId)
    {
        this.windowId = windowId;
    }
}
