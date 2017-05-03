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

import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.deltaspike.core.impl.future.FutureableInterceptor;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;

@RunWith(Arquillian.class)
public class FutureableTest {
    @Deployment
    public static WebArchive deploy()
    {
        // create beans.xml with added interceptor
        BeansDescriptor beans = Descriptors.create(BeansDescriptor.class);
        beans.getOrCreateInterceptors().clazz(FutureableInterceptor.class.getName());
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "FutureableTest.jar")
                .addPackage(Service.class.getPackage().getName())
                .addAsManifestResource(new StringAsset(beans.exportAsString()), "beans.xml");

        return ShrinkWrap.create(WebArchive.class, "FutureableTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private Service service;
    
    @Test
    public void voidTest()
    {
        CountDownLatch latch = new CountDownLatch(1);
        service.thatSLong(1000, latch);
        try
        {
            if (!latch.await(2000, TimeUnit.MILLISECONDS)) {
                fail("Asynchronous call should have terminated");
            }
        }
        catch (final InterruptedException e)
        {
            Thread.interrupted();
            fail();
        }
    }

    @Test
    public void future()
    {
        final Future<String> future = service.thatSLong(1000);
        int count = 0;
        for (int i = 0; i < 1000; i++)
        {
            if (future.isDone())
            {
                break;
            }
            count++;
        }
        try
        {
            assertEquals("done", future.get());
        }
        catch (final InterruptedException e)
        {
            Thread.interrupted();
            fail();
        }
        catch (final ExecutionException e)
        {
            fail(e.getMessage());
        }
        assertEquals(1000, count);
    }
}
