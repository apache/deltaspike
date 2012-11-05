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
package org.apache.deltaspike.core.spi.scope.window;

/**
 * <p>We support the general notion of multiple 'windows'
 * That might be different parallel edit pages in a
 * desktop application (think about different open documents
 * in an editor) or multiple browser tabs in a
 * web application.</p>
 * <p>For web applications each browser tab or window will be
 * represented by an own {@link WindowContext}. All those
 * {@link WindowContext}s will be held in the users servlet
 * session as &#064;SessionScoped bean.
 * </p>
 * <p>Every WindowContext is uniquely identified via a
 * 'windowId'. Each Thread is associated with at most
 * one single windowId at a time. The {@link WindowContextManager}
 * is the interface which allows resolving the current <i>windowId</i>
 * associated with this very Thread.</p>
 */
public interface WindowContextManager
{
    /**
     * @return the <i>windowId</i> associated with the very Thread or <code>null</code>.
     */
    String getCurrentWindowId();

    /**
     * Set the current windowId as the currently active for the very Thread.
     * If no WindowContext exists with the very windowId we will create a new one.
     * @param windowId
     */
    void activateWindowContext(String windowId);

    /**
     * @return the {@link WindowContext} associated with the very Thread or <code>null</code>.
     */
    WindowContext getCurrentWindowContext();

    /**
     * close the WindowContext with the currently activated windowId for the very Thread.
     * @return <code>true</code> if any did exist, <code>false</code> otherwise
     */
    boolean closeCurrentWindowContext();


    /**
     * Close all WindowContexts which are managed by the WindowContextManager.
     * This is necessary when the session gets closed down.
     * @return
     */
    void closeAllWindowContexts();


}
