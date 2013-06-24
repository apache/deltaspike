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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.deltaspike.core.api.provider.BeanProvider;

/**
 * Simple servlet that logs details about the servlet objects injected into {@link ServletObjectInjectionBean}.
 */
public class ServletObjectInjectionServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {

        /*
         * The ServletObjectInjectionBean is manually looked up using BeanProvider because not all containers may
         * support injection into servlets.
         */
        ServletObjectInjectionBean bean =
                BeanProvider.getContextualReference(ServletObjectInjectionBean.class);

        ServletOutputStream stream = resp.getOutputStream();
        logDetails(stream, "ServletRequest", bean.getServletRequest());
        logDetails(stream, "HttpServletRequest", bean.getHttpServletRequest());
        logDetails(stream, "ServletResponse", bean.getServletResponse());
        logDetails(stream, "HttpServletResponse", bean.getHttpServletResponse());
        logDetails(stream, "HttpSession", bean.getHttpSession());
        logDetails(stream, "Principal", bean.getPrincipal());

    }

    /**
     * Writes debug information to the supplied {@link ServletOutputStream}.
     */
    private void logDetails(ServletOutputStream stream, String name, Object obj)
            throws IOException
    {
        stream.print("[");
        stream.print(name);
        stream.print("=");
        stream.print(obj != null ? "OK" : "null");
        stream.print("]\n");
    }

}
