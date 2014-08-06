/*******************************************************************************
 * Copyright (c) 2013 - 2014 Sparta Systems, Inc.
 ******************************************************************************/

package org.apache.deltaspike.cdise.servlet.test.ut;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import org.apache.deltaspike.cdise.servlet.CdiServletRequestListener;
import org.apache.deltaspike.cdise.servlet.test.EmbeddedServletContainer;
import org.apache.deltaspike.cdise.servlet.test.content.RequestServlet;

/**
 * Test execution for undertow embedded container.
 */
public class UndertowTest extends EmbeddedServletContainer
{
    private Undertow server;

    protected int createServer() throws Exception
    {
        int port = getPort();
        ServletInfo servletInfo = Servlets.servlet("RequestServlet", RequestServlet.class).setAsyncSupported(true)
                .setLoadOnStartup(1).addMapping("/*");
        ListenerInfo listenerInfo = Servlets.listener(CdiServletRequestListener.class);
        DeploymentInfo di = new DeploymentInfo()
                .addListener(listenerInfo)
                .setContextPath("/")
                .addServlet(servletInfo).setDeploymentName("CdiSEServlet")
                .setClassLoader(ClassLoader.getSystemClassLoader());
        DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(di);
        deploymentManager.deploy();
        server = Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(deploymentManager.start())
                .build();
        server.start();
        return port;
    }

    protected void shutdown() throws Exception
    {
        server.stop();
    }
}
