/*******************************************************************************
 * Copyright (c) 2013 - 2014 Sparta Systems, Inc.
 ******************************************************************************/

package org.apache.deltaspike.cdise.servlet.test.jetty;

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
