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
package org.apache.deltaspike.test.core.api.config;

import java.util.function.BiFunction;

import org.apache.deltaspike.core.api.config.Config;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.test.core.api.config.beans.ServerEndpointPojoWithCt;
import org.apache.deltaspike.test.core.api.config.beans.ServerEndpointPojoWithFields;
import org.junit.Assert;
import org.junit.Test;

public class BeanConfigResolverTest
{
    @Test
    public void testBeanConverterConfig()
    {
        final BiFunction<Config, String, ServerEndpointPojoWithCt> serverBeanConverter = (cfg, path) -> new ServerEndpointPojoWithCt(
            cfg.resolve(path + "host").getValue(),
            cfg.resolve(path + "port").as(Integer.class).getValue(),
            cfg.resolve(path + "path").getValue());

        final ServerEndpointPojoWithCt someServer = ConfigResolver.resolve("myapp.some.server")
            .asBean(ServerEndpointPojoWithCt.class, serverBeanConverter)
            .getValue();
        Assert.assertNotNull(someServer);
        Assert.assertEquals("http://myserver:80/myapp/endpoint1", someServer.toString());

        final ServerEndpointPojoWithCt otherServer = ConfigResolver.resolve("myapp.other.server")
            .asBean(ServerEndpointPojoWithCt.class, serverBeanConverter)
            .getValue();
        Assert.assertNotNull(otherServer);
        Assert.assertEquals("https://otherserver:443/otherapp/endpoint2", otherServer.toString());
    }

    @Test
    public void testConfigBeanWithCt()
    {
        final ServerEndpointPojoWithCt someServer = ConfigResolver.resolve("myapp.some.server")
            .asBean(ServerEndpointPojoWithCt.class)
            .getValue();
        Assert.assertNotNull(someServer);
        Assert.assertEquals("http://myserver:80/myapp/endpoint1", someServer.toString());
    }

    @Test
    public void testConfigBeanWithFields()
    {
        final ServerEndpointPojoWithFields someServer = ConfigResolver.resolve("myapp.some.server")
            .asBean(ServerEndpointPojoWithFields.class)
            .getValue();
        Assert.assertNotNull(someServer);
        Assert.assertEquals("http://myserver:80/myapp/endpoint1", someServer.toString());
    }


}
