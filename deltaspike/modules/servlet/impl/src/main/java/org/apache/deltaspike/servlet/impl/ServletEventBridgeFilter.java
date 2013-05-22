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

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.servlet.api.literal.DestroyedLiteral;
import org.apache.deltaspike.servlet.api.literal.InitializedLiteral;
import org.apache.deltaspike.servlet.api.literal.WebLiteral;

/**
 * @author Christian Kaltepoth
 */
public class ServletEventBridgeFilter implements Filter
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

        fireEvent(request, WebLiteral.INSTANCE, InitializedLiteral.INSTANCE);
        fireEvent(response, WebLiteral.INSTANCE, InitializedLiteral.INSTANCE);

        try
        {
            chain.doFilter(request, response);
        }
        finally
        {
            fireEvent(request, WebLiteral.INSTANCE, DestroyedLiteral.INSTANCE);
            fireEvent(response, WebLiteral.INSTANCE, DestroyedLiteral.INSTANCE);
        }

    }

    @Override
    public void destroy()
    {
        // nothing yet
    }

    protected void fireEvent(Object event, Annotation... qualifier)
    {
        /*
         * No need to cache the BeanManager reference because the providers already does this on a context class loader
         * level.
         */
        BeanManagerProvider.getInstance().getBeanManager().fireEvent(event, qualifier);
    }

}
