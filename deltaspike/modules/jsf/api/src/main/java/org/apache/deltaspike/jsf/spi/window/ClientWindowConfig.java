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
package org.apache.deltaspike.jsf.spi.window;

import javax.faces.context.FacesContext;

/**
 * Configuration for ClientWindow handler which is used
 * to determine the correct windowId for &#063;WindowScoped beans.
 */
public interface ClientWindowConfig
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
        LAZY

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

}
