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

import org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent;
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
    @Inject
    private CalledExceptionHandler calledExceptionHandler;

    @Deployment(name = "CallingHandlersTest")
    public static Archive<?> createTestArchive()
    {
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
        bm.fireEvent(new ExceptionToCatchEvent(new IllegalArgumentException()));

        assertTrue(calledExceptionHandler.isOutboundHandlerCalled());
    }

    @Test
    public void assertOutboundHanldersAreCalledOnce()
    {
        calledExceptionHandler.setOutboundHandlerTimesCalled(0);
        bm.fireEvent(new ExceptionToCatchEvent(new IllegalArgumentException()));
        assertEquals(1, calledExceptionHandler.getOutboundHandlerTimesCalled());
    }

    @Test
    public void assertInboundHanldersAreCalledOnce()
    {
        calledExceptionHandler.setInboundHandlerTimesCalled(0);
        bm.fireEvent(new ExceptionToCatchEvent(new IllegalArgumentException()));
        assertEquals(1, calledExceptionHandler.getInboundHandlerTimesCalled());
    }

    @Test
    public void assertAdditionalParamsAreInjected()
    {
        bm.fireEvent(new ExceptionToCatchEvent(new RuntimeException(new IllegalArgumentException())));
        assertTrue(calledExceptionHandler.isBeanmanagerInjected());
    }

    //@Test //TODO discuss this test
    public void assertAdditionalParamsAreInjectedWithDifferentHandlerLocation()
    {
        bm.fireEvent(new ExceptionToCatchEvent(new SQLException()));
        assertTrue(calledExceptionHandler.isLocationDifferBeanmanagerInjected());
    }

    @Test
    public void assertProtectedHandlersAreCalled()
    {
        bm.fireEvent(new ExceptionToCatchEvent(new IllegalStateException()));
        assertTrue(calledExceptionHandler.isProtectedHandlerCalled());
    }
}
