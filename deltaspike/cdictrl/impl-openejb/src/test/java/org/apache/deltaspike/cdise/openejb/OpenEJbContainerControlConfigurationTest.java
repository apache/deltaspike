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
package org.apache.deltaspike.cdise.openejb;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.openejb.bean.Foo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.Test;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OpenEJbContainerControlConfigurationTest
{
    @Test
    public void ensureDataSourceExist()
    {
        final CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.boot(new HashMap<Object, Object>()
        {{
            put("foo", "new://Resource?type=DataSource");
            put("foo.JdbcUrl", "jdbc:hsqldb:mem:foo");
            put("foo.JtaManaged", "false");
        }});

        try
        {
            final DataSource ds = DataSource.class.cast(SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup("java:openejb/Resource/foo"));
            Connection c = null;
            try {
                c = ds.getConnection();
                assertEquals("jdbc:hsqldb:mem:foo", c.getMetaData().getURL());
            }
            catch (SQLException e)
            {
                fail(e.getMessage());
            }
            finally
            {
                try
                {
                    if (c != null) {
                        c.close();
                    }
                }
                catch (SQLException e)
                {
                    // no-op
                }
            }
        }
        catch (final NamingException e)
        {
            fail(e.getMessage());
        }
        finally
        {
            container.shutdown();
        }
    }

    @Test
    public void basicInjection() // useless because of tcks but nice to have when working on this specific container
    {
        final CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.boot();

        try
        {
            final BeanManager beanManager = container.getBeanManager();
            assertEquals("foo", Foo.class.cast(beanManager.getReference(beanManager.resolve(beanManager.getBeans(Foo.class)), Foo.class, null)).name());
        }
        finally
        {
            container.shutdown();
        }
    }
}
