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
package org.apache.deltaspike.servlet.impl.produce;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Utility class which is used to bind the {@link ServletRequest} and {@link ServletResponse} to the current thread.
 * 
 * @param <Type>
 *            {@link ServletRequest} or {@link ServletResponse}
 */
class RequestResponseHolder<Type>
{

    /**
     * Instance for storing the {@link ServletRequest}
     */
    static final RequestResponseHolder<ServletRequest> REQUEST = new RequestResponseHolder<ServletRequest>();

    /**
     * Instance for storing the {@link ServletResponse}
     */
    static final RequestResponseHolder<ServletResponse> RESPONSE = new RequestResponseHolder<ServletResponse>();

    private final ThreadLocal<Type> threadLocal = new ThreadLocal<Type>();

    private RequestResponseHolder()
    {
        // hide constructor
    }

    /**
     * Binds the request or response to the current thread.
     * 
     * @param instance
     *            The request/response
     * @throws IllegalStateException
     *             if there is already an instance bound to the thread
     */
    void bind(Type instance)
    {
        if (isBound())
        {
            // ignore forwards - Tomcat calls #requestInitialized two times with form authentication
            if (instance instanceof ServletRequest)
            {
                ServletRequest servletRequest = (ServletRequest) instance;
                if (servletRequest.getAttribute("javax.servlet.forward.request_uri") != null)
                {
                    return;
                }
            }

            throw new IllegalStateException("There is already an instance bound to this thread.");
        }
        threadLocal.set(instance);
    }

    /**
     * Returns <code>true</code> if there is already an instance bound to the thread
     */
    boolean isBound()
    {
        return threadLocal.get() != null;
    }

    /**
     * Release the instance bound to the current thread
     */
    void release()
    {
        threadLocal.remove();
    }

    /**
     * Retrieve the request/response bound to the current thread.
     * 
     * @return instance bound to the thread
     * @throws IllegalStateException
     *             if there is no instance bound to the thread
     */
    Type get()
    {
        Type instance = threadLocal.get();
        if (instance == null)
        {
            throw new IllegalStateException("Attempt to access the request/response without an active HTTP request");
        }
        return instance;
    }

}
