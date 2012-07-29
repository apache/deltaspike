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
package org.apache.deltaspike.test.jpa.datasource;

import org.apache.deltaspike.jpa.api.datasource.DataSourceConfig;
import org.apache.deltaspike.jpa.impl.datasource.ConfigurableDataSource;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionContextExtension;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Assert;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import java.sql.Connection;

@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class ConfigurableDataSourceTest
{

    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "defaultInjectionTest.jar")
                .addPackage(ArchiveUtils.SHARED_PACKAGE)
                .addPackage(LocalDatabaseConfig.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJpaArchive())
                .addAsLibraries(testJar)
                .addAsServiceProvider(Extension.class, TransactionContextExtension.class)
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }

    @Inject
    private DataSourceConfig dataSourceConfig;

    @Test
    public void testLocalDataSource() throws Exception
    {
        Assert.assertNull(dataSourceConfig.getJndiResourceName(null));
        Assert.assertEquals("org.apache.deltaspike.test.jpa.datasource.DummyJdbcDriver",
                dataSourceConfig.getConnectionClassName(null));
    }

    @Test
    public void testConfigurableDataSource() throws Exception
    {
        // we do not use @Inject as this is normally instantiated
        // via newInstance()
        ConfigurableDataSource cds = new ConfigurableDataSource();
        Connection connection = cds.getConnection();
        Assert.assertNotNull(connection);
    }
}
