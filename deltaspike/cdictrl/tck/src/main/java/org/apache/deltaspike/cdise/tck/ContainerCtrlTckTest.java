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
package org.apache.deltaspike.cdise.tck;


import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.cdise.tck.beans.Car;
import org.apache.deltaspike.cdise.tck.beans.CarRepair;
import org.apache.deltaspike.cdise.tck.beans.TestUser;
import org.apache.deltaspike.cdise.tck.control.LockedCDIImplementation;
import org.apache.deltaspike.cdise.tck.control.LockedVersionRange;
import org.apache.deltaspike.cdise.tck.control.VersionControlRule;
import org.apache.deltaspike.test.utils.CdiImplementation;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * TCK test for the {@link org.apache.deltaspike.cdise.api.CdiContainer}
 */
public class ContainerCtrlTckTest
{
    private static final Logger log = Logger.getLogger(ContainerCtrlTckTest.class.getName());
    private static final int NUM_THREADS = 10;

    @Rule
    public VersionControlRule versionControlRule = new VersionControlRule();

    @Test
    public void testContainerBoot()
    {
        CdiContainer cc = CdiContainerLoader.getCdiContainer();
        Assert.assertNotNull(cc);

        cc.boot();
        cc.getContextControl().startContexts();

        BeanManager bm = cc.getBeanManager();
        Assert.assertNotNull(bm);
        
        Set<Bean<?>> beans = bm.getBeans(CarRepair.class);
        Bean<?> bean = bm.resolve(beans);
        
        CarRepair carRepair = (CarRepair) bm.getReference(bean, CarRepair.class, bm.createCreationalContext(bean));
        Assert.assertNotNull(carRepair);

        Assert.assertNotNull(carRepair.getCar());
        Assert.assertNotNull(carRepair.getCar().getUser());

        cc.shutdown();
    }

    @Test
    public void testParallelThreadExecution() throws Exception
    {
        final CdiContainer cc = CdiContainerLoader.getCdiContainer();
        Assert.assertNotNull(cc);

        cc.boot();
        cc.getContextControl().startContexts();

        final BeanManager bm = cc.getBeanManager();
        Assert.assertNotNull(bm);

        final AtomicInteger numErrors = new AtomicInteger(0);
        final ContextControl contextControl = cc.getContextControl();

        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    contextControl.startContext(SessionScoped.class);
                    contextControl.startContext(RequestScoped.class);


                    Set<Bean<?>> beans = bm.getBeans(CarRepair.class);
                    Bean<?> bean = bm.resolve(beans);

                    CarRepair carRepair = (CarRepair)
                            bm.getReference(bean, CarRepair.class, bm.createCreationalContext(bean));
                    Assert.assertNotNull(carRepair);

                    for (int i = 0; i < 100000; i++)
                    {
                        // we need the threads doing something ;)
                        Assert.assertNotNull(carRepair.getCar());
                        Assert.assertNotNull(carRepair.getCar().getUser());
                        Assert.assertNull(carRepair.getCar().getUser().getName());
                    }
                    contextControl.stopContext(RequestScoped.class);
                    contextControl.stopContext(SessionScoped.class);
                }
                catch (Throwable e)
                {
                    log.log(Level.SEVERE, "An exception happened on a new worker thread", e);
                    numErrors.incrementAndGet();
                }
            }
        };


        Thread[] threads = new Thread[NUM_THREADS];
        for (int i = 0 ; i < NUM_THREADS; i++)
        {
            threads[i] = new Thread(runnable);
        }

        for (int i = 0 ; i < NUM_THREADS; i++)
        {
            threads[i].start();
        }

        for (int i = 0 ; i < NUM_THREADS; i++)
        {
            threads[i].join();
        }

        Assert.assertEquals("An error happened while executing parallel threads", 0, numErrors.get());


        cc.shutdown();
    }

    /**
     * Stops and starts: application-, session- and request-scope.
     * <p/>
     * application-scoped instance has a ref to
     * request-scoped instance which has a ref to
     * session-scoped instance.
     * <p/>
     * If the deepest ref has the expected value, all levels in between were resetted correctly.
     */
    @Test
    public void testRestartContexts()
    {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        Assert.assertNotNull(cdiContainer);

        cdiContainer.boot();
        cdiContainer.getContextControl().startContexts();

        BeanManager beanManager = cdiContainer.getBeanManager();
        Assert.assertNotNull(beanManager);

        Set<Bean<?>> beans = beanManager.getBeans(CarRepair.class);
        Bean<?> bean = beanManager.resolve(beans);

        CarRepair carRepair = (CarRepair)
            beanManager.getReference(bean, CarRepair.class, beanManager.createCreationalContext(bean));

        Assert.assertNotNull(carRepair);

        Car car = carRepair.getCar();

        Assert.assertNotNull(car);
        Assert.assertNotNull(car.getUser());


        carRepair.getCar().getUser().setName("tester");
        Assert.assertEquals("tester", car.getUser().getName());

        Assert.assertFalse(CarRepair.isPreDestroyCalled());
        Assert.assertFalse(Car.isPreDestroyCalled());
        Assert.assertFalse(TestUser.isPreDestroyCalled());

        cdiContainer.getContextControl().stopContexts();

        Assert.assertTrue(CarRepair.isPreDestroyCalled());
        Assert.assertTrue(Car.isPreDestroyCalled());
        Assert.assertTrue(TestUser.isPreDestroyCalled());

        try
        {
            car.getUser();

            // accessing the car should have triggered a ContextNotActiveException
            Assert.fail();
        }
        catch (ContextNotActiveException e)
        {
            //do nothing - exception expected
        }

        cdiContainer.getContextControl().startContexts();

        carRepair = (CarRepair)
            beanManager.getReference(bean, CarRepair.class, beanManager.createCreationalContext(bean));

        Assert.assertNotNull(carRepair.getCar());
        Assert.assertNotNull(carRepair.getCar().getUser());
        Assert.assertNull(carRepair.getCar().getUser().getName());

        cdiContainer.shutdown();
    }

    @LockedCDIImplementation(versions = {
            @LockedVersionRange(implementation = CdiImplementation.WELD11, versionRange = "[1.1.14,1.2)"),
            @LockedVersionRange(implementation = CdiImplementation.WELD20, versionRange = "[2.0.1.Final,2.1)")
        })
    @Test
    public void testShutdownWithInactiveContexts()
    {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        Assert.assertNotNull(cdiContainer);

        cdiContainer.boot();
        cdiContainer.getContextControl().startContexts();

        // now do some random stuff
        BeanManager beanManager = cdiContainer.getBeanManager();
        Assert.assertNotNull(beanManager);

        Set<Bean<?>> beans = beanManager.getBeans(CarRepair.class);
        Bean<?> bean = beanManager.resolve(beans);

        CarRepair carRepair = (CarRepair)
                beanManager.getReference(bean, CarRepair.class, beanManager.createCreationalContext(bean));

        Assert.assertNotNull(carRepair);

        Car car = carRepair.getCar();

        Assert.assertNotNull(car);
        Assert.assertNotNull(car.getUser());


        carRepair.getCar().getUser().setName("tester");
        Assert.assertEquals("tester", car.getUser().getName());

        Assert.assertFalse(CarRepair.isPreDestroyCalled());
        Assert.assertFalse(Car.isPreDestroyCalled());
        Assert.assertFalse(TestUser.isPreDestroyCalled());

        cdiContainer.getContextControl().stopContexts();

        Assert.assertTrue(CarRepair.isPreDestroyCalled());
        Assert.assertTrue(Car.isPreDestroyCalled());
        Assert.assertTrue(TestUser.isPreDestroyCalled());

        cdiContainer.shutdown();
    }

    @Test
    public void testNewRequests()
    {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        Assert.assertNotNull(cdiContainer);

        cdiContainer.boot();
        cdiContainer.getContextControl().startContext(SessionScoped.class);
        cdiContainer.getContextControl().startContext(RequestScoped.class);

        BeanManager beanManager = cdiContainer.getBeanManager();
        Assert.assertNotNull(beanManager);

        TestUser testUser = resolveInstance(beanManager, TestUser.class);

        Assert.assertNotNull(testUser);
        testUser.setName("tester");


        CarRepair carRepair = resolveInstance(beanManager, CarRepair.class);

        Assert.assertNotNull(carRepair);

        Car car = carRepair.getCar();

        Assert.assertNotNull(car);
        Assert.assertNotNull(car.getUser());
        Assert.assertEquals("tester", car.getUser().getName());


        carRepair.getCar().getUser().setName("tck-tester");
        Assert.assertEquals("tck-tester", testUser.getName());

        cdiContainer.getContextControl().stopContext(RequestScoped.class);
        cdiContainer.getContextControl().startContext(RequestScoped.class);

        try
        {
            testUser = resolveInstance(beanManager, TestUser.class);

            Assert.assertNotNull(testUser);
            Assert.assertNotNull(testUser.getName());
            Assert.assertEquals("tck-tester", testUser.getName());
        }
        catch (ContextNotActiveException e)
        {
            Assert.fail(e.getMessage());
        }

        try
        {
            carRepair = resolveInstance(beanManager, CarRepair.class);

            Assert.assertNotNull(carRepair);

            car = carRepair.getCar();

            Assert.assertNotNull(car);
            Assert.assertNotNull(car.getUser());
            Assert.assertNotNull(car.getUser().getName());
            Assert.assertEquals("tck-tester", car.getUser().getName());
        }
        catch (ContextNotActiveException e)
        {
            Assert.fail(e.getMessage());
        }

        cdiContainer.shutdown();
    }

    private <T> T resolveInstance(BeanManager beanManager, Class<T> beanClass)
    {
        Set<Bean<?>> beans = beanManager.getBeans(beanClass);
        Bean<?> bean = beanManager.resolve(beans);

        return (T) beanManager.getReference(bean, beanClass, beanManager.createCreationalContext(bean));
    }
}
