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
