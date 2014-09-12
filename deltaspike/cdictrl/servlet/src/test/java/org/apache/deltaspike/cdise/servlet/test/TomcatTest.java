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

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.deltaspike.cdise.servlet.CdiServletContextListener;
import org.apache.deltaspike.cdise.servlet.test.content.RequestServlet;

import java.io.File;

/**
 * Embedded servlet tests for Tomcat.
 */
public class TomcatTest extends EmbeddedServletContainer
{
    private Tomcat tomcat;
    @Override
    protected int createServer() throws Exception
    {
        String baseDir = "target/webapp-runner";
        tomcat = new Tomcat();
        int port = super.getPort();
        tomcat.setPort(port);
        File base = new File(baseDir);
        if (!base.exists())
        {
            base.mkdirs();
        }
        tomcat.setBaseDir(baseDir);
        Context ctx = tomcat.addContext("/",base.getAbsolutePath());
        StandardContext standardContext = (StandardContext)ctx;
        standardContext.addApplicationListener(CdiServletContextListener.class.getName());

        Wrapper wrapper = Tomcat.addServlet(ctx,"RequestServlet",RequestServlet.class.getName());
        wrapper.addMapping("/*");
        tomcat.start();
        return port;
    }

    @Override
    protected void shutdown() throws Exception
    {
        tomcat.stop();
    }
}
