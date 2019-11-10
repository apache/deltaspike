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
package org.apache.deltaspike.test.core.impl.future;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.impl.config.PropertiesConfigSource;
import org.apache.deltaspike.core.impl.future.ThreadPoolManager;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.apache.deltaspike.test.util.ArchiveUtils;

@RunWith(Arquillian.class)
public class ThreadPoolManagerTest
{
    @Deployment
    public static WebArchive deploy()
    {
        return ShrinkWrap.create(WebArchive.class, "ThreadPoolManagerTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private ThreadPoolManager manager;

    @Test
    public void defaultPool() throws ExecutionException, InterruptedException
    {
        final ExecutorService auto = manager.find("auto");
        assertEquals(auto, auto);
        assertSame(auto, auto);
        assertEquals(Runtime.getRuntime().availableProcessors(), ThreadPoolExecutor.class.cast(auto).getCorePoolSize());
        assertUsable(auto);
    }

    @Test // this test validates we read the config properly but also it is lazy
    public void configuredPool() throws ExecutionException, InterruptedException
    {
        ConfigResolver.addConfigSources(Collections.<ConfigSource>singletonList(new PropertiesConfigSource(new Properties()
        {{
            setProperty("futureable.pool.custom.coreSize", "5");
        }})
        {
            @Override
            public String getConfigName()
            {
                return "configuredPool";
            }
        }));
        final ExecutorService custom = manager.find("custom");
        assertEquals(custom, custom);
        assertSame(custom, custom);
        assertEquals(5, ThreadPoolExecutor.class.cast(custom).getCorePoolSize());
        assertUsable(custom);
    }

    private void assertUsable(final ExecutorService pool) throws InterruptedException, ExecutionException
    {
        assertEquals("ok", pool.submit(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "ok";
            }
        }).get());
    }
}
