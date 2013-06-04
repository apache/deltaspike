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

import java.security.Principal;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.deltaspike.servlet.api.Web;

/**
 * Producer for standard servlet objects. All objects are produced with the {@link Web} qualifier. Currently the
 * following objects are supported:
 * 
 * <ul>
 * <li>{@link ServletContext}</li>
 * <li>{@link ServletRequest}</li>
 * <li>{@link HttpServletRequest}</li>
 * <li>{@link ServletResponse}</li>
 * <li>{@link HttpServletResponse}</li>
 * <li>{@link HttpSession}</li>
 * <li>{@link Principal}</li>
 * </ul>
 * 
 * @author Christian Kaltepoth
 */
public class ServletObjectProducer
{

    @Produces
    @Web
    public ServletContext getServletContext()
    {
        return ServletContextHolder.get();
    }

    @Produces
    @Web
    @RequestScoped
    public ServletRequest getServletRequest()
    {
        return RequestResponseHolder.get().getRequest();
    }

    @Produces
    @Typed(HttpServletRequest.class)
    @Web
    @RequestScoped
    public HttpServletRequest getHttpServletRequest()
    {
        ServletRequest request = RequestResponseHolder.get().getRequest();
        if (request instanceof HttpServletRequest)
        {
            return (HttpServletRequest) request;
        }
        throw new IllegalStateException("The current request is not a HttpServletRequest");
    }

    @Produces
    @Web
    @RequestScoped
    public ServletResponse getServletResponse()
    {
        return RequestResponseHolder.get().getResponse();
    }

    @Produces
    @Typed(HttpServletResponse.class)
    @Web
    @RequestScoped
    public HttpServletResponse getHttpServletResponse()
    {
        ServletResponse response = RequestResponseHolder.get().getResponse();
        if (response instanceof HttpServletResponse)
        {
            return (HttpServletResponse) response;
        }
        throw new IllegalStateException("The current response is not a HttpServletResponse");
    }

    @Produces
    @Web
    @SessionScoped
    public HttpSession getHttpSession()
    {
        ServletRequest request = RequestResponseHolder.get().getRequest();
        if (request instanceof HttpServletRequest)
        {
            return ((HttpServletRequest) request).getSession(true);
        }
        throw new IllegalStateException(
                "Cannot produce HttpSession because the current request is not a HttpServletRequest");
    }

    @Produces
    @Web
    @RequestScoped
    public Principal getPrincipal()
    {
        return getHttpServletRequest().getUserPrincipal();
    }

}
