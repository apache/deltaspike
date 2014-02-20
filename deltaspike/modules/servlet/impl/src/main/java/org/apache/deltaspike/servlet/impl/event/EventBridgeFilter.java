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
package org.apache.deltaspike.servlet.impl.event;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.deltaspike.core.api.literal.DestroyedLiteral;
import org.apache.deltaspike.core.api.literal.InitializedLiteral;

/**
 * This filter sends events to the CDI event bus when requests and responses get created and destroyed.
 */
public class EventBridgeFilter extends EventBroadcaster implements Filter
{

    @Override
    public void init(FilterConfig config) throws ServletException
    {
        // nothing yet
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException
    {

        // fire @Initialized events
        if (isActivated())
        {
            fireEvent(request, InitializedLiteral.INSTANCE);
            fireEvent(response, InitializedLiteral.INSTANCE);
        }

        try
        {
            chain.doFilter(request, response);
        }
        finally
        {
            // fire @Destroyed events
            if (isActivated())
            {
                fireEvent(request, DestroyedLiteral.INSTANCE);
                fireEvent(response, DestroyedLiteral.INSTANCE);
            }
        }

    }

    @Override
    public void destroy()
    {
        // nothing yet
    }

}
