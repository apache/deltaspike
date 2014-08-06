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
package org.apache.deltaspike.cdise.servlet.test;

import org.apache.deltaspike.cdise.servlet.CdiServletRequestListener;
import org.apache.deltaspike.cdise.servlet.test.EmbeddedServletContainer;
import org.apache.deltaspike.cdise.servlet.test.content.RequestServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Jetty based tests.
 */
public class JettyTest extends EmbeddedServletContainer
{
    private Server server;
    @Override
    protected int createServer() throws Exception
    {
        int port = super.getPort();
        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addEventListener(new CdiServletRequestListener());
        context.addServlet(new ServletHolder(new RequestServlet()),"/*");

        server.start();
        return port;
    }

    @Override
    protected void shutdown() throws Exception
    {
        server.stop();
    }
}
