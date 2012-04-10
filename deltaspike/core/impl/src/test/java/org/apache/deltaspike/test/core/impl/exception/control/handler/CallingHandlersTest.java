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

package org.apache.deltaspike.test.core.impl.exception.control.handler;

import org.apache.deltaspike.core.api.exception.control.ExceptionToCatch;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class CallingHandlersTest
{
    @Deployment(name = "CallingHandlersTest")
    public static Archive<?> createTestArchive()
    {
        new BeanManagerProvider()
        {
            @Override
            public void setTestMode()
            {
                super.setTestMode();
            }
        }.setTestMode();

        return ShrinkWrap
                .create(WebArchive.class, "callingHandlers.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addClasses(CalledExceptionHandler.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private BeanManager bm;

    @Test
    public void assertOutboundHanldersAreCalled()
    {
        bm.fireEvent(new ExceptionToCatch(new IllegalArgumentException()));

        assertTrue(CalledExceptionHandler.OUTBOUND_HANDLER_CALLED);
    }

    @Test
    public void assertOutboundHanldersAreCalledOnce()
    {
        CalledExceptionHandler.OUTBOUND_HANDLER_TIMES_CALLED = 0;
        bm.fireEvent(new ExceptionToCatch(new IllegalArgumentException()));
        assertEquals(1, CalledExceptionHandler.OUTBOUND_HANDLER_TIMES_CALLED);
    }

    @Test
    public void assertInboundHanldersAreCalledOnce()
    {
        CalledExceptionHandler.INBOUND_HANDLER_TIMES_CALLED = 0;
        bm.fireEvent(new ExceptionToCatch(new IllegalArgumentException()));
        assertEquals(1, CalledExceptionHandler.INBOUND_HANDLER_TIMES_CALLED);
    }

    @Test
    public void assertAdditionalParamsAreInjected()
    {
        bm.fireEvent(new ExceptionToCatch(new RuntimeException(new IllegalArgumentException())));
        assertTrue(CalledExceptionHandler.BEANMANAGER_INJECTED);
    }

    @Test
    public void assertAdditionalParamsAreInjectedWithDifferentHandlerLocation()
    {
        bm.fireEvent(new ExceptionToCatch(new SQLException()));
        assertTrue(CalledExceptionHandler.LOCATION_DIFFER_BEANMANAGER_INJECTED);
    }

    @Test
    public void assertProtectedHandlersAreCalled()
    {
        bm.fireEvent(new ExceptionToCatch(new IllegalStateException()));
        assertTrue(CalledExceptionHandler.PROTECTED_HANDLER_CALLED);
    }
}
