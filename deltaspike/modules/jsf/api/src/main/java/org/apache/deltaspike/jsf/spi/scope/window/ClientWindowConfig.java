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
package org.apache.deltaspike.jsf.spi.scope.window;

import javax.faces.context.FacesContext;
import java.io.Serializable;

/**
 * Configuration for ClientWindow handler which is used
 * to determine the correct windowId for &#063;WindowScoped beans.
 */
public interface ClientWindowConfig extends Serializable
{
    public enum ClientWindowRenderMode
    {
        /**
         * Any window or browser tab detection is disabled for this request
         */
        NONE,

        /**
         * <p>The GET request results in an intermediate small html page which
         * checks if the browser tab fits the selected windowId</p>
         * <p>The ClientWindow html extracts the windowId from the window.name and
         * enforces a 2nd GET which will contain the windowId and will get routed
         * through to the target JSF page.</p>
         */
        CLIENTWINDOW,

        /**
         * Render each GET request with the windowId you get during the request
         * and perform a lazy check on the client side via JavaScript or similar.
         */
        LAZY,

        /**
         * Delegates to the default window-handling of JSF 2.2+ (if configured)
         */
        DELEGATED,

        /**
         * If you set this mode, you also need to provide an own {@link ClientWindow} implementation.
         */
        CUSTOM

    }

    /**
     * @return whether JavaScript is enabled
     */
    boolean isJavaScriptEnabled();

    /**
     * @param javaScriptEnabled whether JavaScript is enabled
     */
    void setJavaScriptEnabled(boolean javaScriptEnabled);

    /**
     * Determine whether this request should take care of clientWindow detection.
     * This can e.g. get disabled for download pages or if a useragent doesn't
     * support html5 or any other required technique.
     * This only gets checked for GET requests!
     *
     * @param facesContext
     * @return the selected ClientWindowRenderMode
     */
    ClientWindowRenderMode getClientWindowRenderMode(FacesContext facesContext);

    /**
     * @return the prepared html which gets sent out to the client as intermediate client window.
     */
    String getClientWindowHtml();

    /**
     * @return Whether the DOM tree should stored in the localStorage for the windowhandler.html
     *         when clicking on a link.
     *         Currently it's only used by {@link ClientWindowRenderMode#CLIENTWINDOW}.
     * @see windowhandler.html
     */
    boolean isClientWindowStoreWindowTreeEnabledOnLinkClick();

    /**
     * @return Whether the DOM tree should stored in the localStorage for the windowhandler.html
     *         when clicking on a button.
     *         Currently it's only used by {@link ClientWindowRenderMode#CLIENTWINDOW}.
     * @see windowhandler.html
     */
    boolean isClientWindowStoreWindowTreeEnabledOnButtonClick();

    boolean isClientWindowTokenizedRedirectEnabled();
    
    /**
     * Restricts the number of active windows.
     *
     * @return limit for active windows
     */
    int getMaxWindowContextCount();
}
