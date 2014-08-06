/*******************************************************************************
 * Copyright (c) 2013 - 2014 Sparta Systems, Inc.
 ******************************************************************************/

package org.apache.deltaspike.cdise.servlet.test;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.servlet.CdiServletRequestListener;
import org.apache.deltaspike.cdise.servlet.test.content.RequestServlet;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by johnament on 8/5/14.
 */
public abstract class EmbeddedServletContainer
{
    protected int getPort()
    {
        Random r = new Random();
        for(int i = 0;i<10;i++)
        {
            int p = r.nextInt(9999);
            if(p > 1000)
            {
                return p;
            }
        }
        return 1001;
    }

    protected abstract int createServer() throws Exception;

    protected abstract void shutdown() throws Exception;

    @Test
    public void testBootRequest() throws Exception
    {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();
        cdiContainer.getContextControl().startContexts();
        int port = createServer();
        testRead(port);
        shutdown();
    }

    private void testRead(int port) throws Exception
    {
        String url = new URL("http","localhost",port,"/").toString();
        HttpResponse response = new DefaultHttpClient().execute(new HttpGet(url));
        assertEquals(200, response.getStatusLine().getStatusCode());
        InputStream is = response.getEntity().getContent();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String data = br.readLine();
        is.close();
        assertEquals("Hello, world!",data);
    }
}
