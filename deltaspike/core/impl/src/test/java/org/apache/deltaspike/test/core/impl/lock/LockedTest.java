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
package org.apache.deltaspike.test.core.impl.lock;

import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.deltaspike.core.impl.lock.LockedInterceptor;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;

@RunWith(Arquillian.class)
public class LockedTest {
    @Deployment
    public static WebArchive deploy()
    {
        // create beans.xml with added interceptor
        BeansDescriptor beans = Descriptors.create(BeansDescriptor.class);
        beans.getOrCreateInterceptors().clazz(LockedInterceptor.class.getName());
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "LockedTest.jar")
                .addPackage(Service.class.getPackage().getName())
                .addAsManifestResource(new StringAsset(beans.exportAsString()), "beans.xml");

        return ShrinkWrap.create(WebArchive.class, "LockedTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(new StringAsset(beans.exportAsString()), "beans.xml");
    }

    @Inject
    private Service service;

    @Test
    public void simpleNotConcurrent()
    {
        final CountDownLatch synchro = new CountDownLatch(1);
        final Thread writer = new Thread()
        {
            @Override
            public void run()
            {
                service.write("test", "value");
                synchro.countDown();
            }
        };

        final CountDownLatch end = new CountDownLatch(1);
        final AtomicReference<String> val = new AtomicReference<String>();
        final Thread reader = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    synchro.await(1, TimeUnit.MINUTES);
                }
                catch (final InterruptedException e)
                {
                    Thread.interrupted();
                    fail();
                }
                val.set(service.read("test"));
                end.countDown();
            }
        };

        reader.start();
        writer.start();
        try
        {
            end.await(1, TimeUnit.MINUTES);
        }
        catch (final InterruptedException e)
        {
            Thread.interrupted();
            fail();
        }
        assertEquals("value", val.get());
    }

    @Test
    public void concurrentTimeout()
    {
        final AtomicBoolean doAgain = new AtomicBoolean(true);
        final CountDownLatch endWriter = new CountDownLatch(1);
        final Thread writer = new Thread()
        {
            @Override
            public void run()
            {
                while (doAgain.get())
                {
                    service.write("test", "value");
                    service.force();
                }
                endWriter.countDown();
            }
        };

        final CountDownLatch endReader = new CountDownLatch(1);
        final Thread reader = new Thread()
        {
            @Override
            public void run()
            {
                while (doAgain.get())
                {
                    try
                    {
                        service.read("test");
                    }
                    catch (final IllegalStateException e)
                    {
                        doAgain.set(false);
                    }
                }
                endReader.countDown();
            }
        };

        reader.start();
        writer.start();
        try
        {
            endReader.await(1, TimeUnit.MINUTES);
            endWriter.await(1, TimeUnit.MINUTES);
        }
        catch (final InterruptedException e)
        {
            Thread.interrupted();
            fail();
        }
        assertEquals("value", service.read("test"));
    }
}
