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
package org.apache.deltaspike.servlet.impl;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * The {@link RequestResponseHolder} holds {@link RequestResponse} objects in a {@link ThreadLocal}.
 * 
 * @author Christian Kaltepoth
 */
class RequestResponseHolder
{

    private static final ThreadLocal<RequestResponse> requestResponseThreadLocal = new ThreadLocal<RequestResponse>();

    private RequestResponseHolder()
    {
        // no instance creation allowed
    }

    /**
     * Binds the a request/response pair to the current thread.
     * 
     * @param request
     *            The request, must not be <code>null</code>
     * @param response
     *            The response, must not be <code>null</code>
     */
    static void bind(ServletRequest request, ServletResponse response)
    {
        bind(new RequestResponse(request, response));
    }

    /**
     * Binds the a request/response pair to the current thread.
     * 
     * @param pair
     *            The request/response pair, must not be <code>null</code>
     */
    static void bind(RequestResponse pair)
    {
        if (requestResponseThreadLocal.get() != null)
        {
            throw new IllegalStateException("There is already an instance stored for this thread.");
        }
        requestResponseThreadLocal.set(pair);
    }

    /**
     * Releases the stored request/response pair for the current thread.
     */
    static void release()
    {
        requestResponseThreadLocal.remove();
    }

    /**
     * Retrieves the request/response pair associated with the current thread.
     * 
     * @return The request/response pair, never <code>null</code>
     * @throws IllegalStateException
     *             if no pair is associated with the thread
     */
    static RequestResponse get()
    {
        RequestResponse requestResponse = requestResponseThreadLocal.get();
        if (requestResponse == null)
        {
            throw new IllegalStateException("Attempt to access the request/response without an active HTTP request");
        }
        return requestResponse;
    }

}
