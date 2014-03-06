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
package org.apache.deltaspike.test.servlet.impl.producer;

import java.security.Principal;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.deltaspike.core.api.common.DeltaSpike;

/**
 * Simple CDI beans with various injection points for servlet ojbects.
 */
public class ServletObjectInjectionBean
{

    @DeltaSpike
    @Inject
    private ServletRequest servletRequest;

    @DeltaSpike
    @Inject
    private HttpServletRequest httpServletRequest;

    @DeltaSpike
    @Inject
    private ServletResponse servletResponse;

    @DeltaSpike
    @Inject
    private HttpServletResponse httpServletResponse;

    @DeltaSpike
    @Inject
    private HttpSession httpSession;

    @DeltaSpike
    @Inject
    private Principal principal;

    public ServletRequest getServletRequest()
    {
        return servletRequest;
    }

    public void setServletRequest(ServletRequest servletRequest)
    {
        this.servletRequest = servletRequest;
    }

    public HttpServletRequest getHttpServletRequest()
    {
        return httpServletRequest;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest)
    {
        this.httpServletRequest = httpServletRequest;
    }

    public ServletResponse getServletResponse()
    {
        return servletResponse;
    }

    public void setServletResponse(ServletResponse servletResponse)
    {
        this.servletResponse = servletResponse;
    }

    public HttpServletResponse getHttpServletResponse()
    {
        return httpServletResponse;
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse)
    {
        this.httpServletResponse = httpServletResponse;
    }

    public HttpSession getHttpSession()
    {
        return httpSession;
    }

    public void setHttpSession(HttpSession httpSession)
    {
        this.httpSession = httpSession;
    }

    public Principal getPrincipal()
    {
        return principal;
    }

    public void setPrincipal(Principal principal)
    {
        this.principal = principal;
    }

}
