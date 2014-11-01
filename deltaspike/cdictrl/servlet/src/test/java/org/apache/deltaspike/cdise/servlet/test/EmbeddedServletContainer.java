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

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
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
 * Base test for testing embedded servlet runtimes.
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

        try
        {
            shutdown();
        }
        finally
        {
            cdiContainer.shutdown(); //also calls #stopContexts
        }
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
