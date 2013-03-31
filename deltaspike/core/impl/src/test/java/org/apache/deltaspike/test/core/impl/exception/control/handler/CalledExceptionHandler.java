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

import org.apache.deltaspike.core.api.exception.control.BeforeHandles;
import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.Handles;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import java.sql.SQLException;

@ApplicationScoped
@ExceptionHandler
public class CalledExceptionHandler
{
    private boolean outboundHandlerCalled = false;
    private int outboundHandlerTimesCalled = 0;
    private boolean protectedHandlerCalled = false;
    private int inboundHandlerTimesCalled = 0;
    private boolean beanmanagerInjected = false;
    private boolean locationDifferBeanmanagerInjected = false;

    public void basicHandler(@Handles ExceptionEvent<Exception> event)
    {
        outboundHandlerCalled = true;
        outboundHandlerTimesCalled++;
    }

    public void basicInboundHandler(@BeforeHandles ExceptionEvent<Exception> event)
    {
        inboundHandlerTimesCalled++;
        event.handledAndContinue();
    }

    public void extraInjections(@Handles ExceptionEvent<IllegalArgumentException> event, BeanManager bm)
    {
        if (bm != null)
        {
            beanmanagerInjected = true;
        }
    }

    void protectedHandler(@Handles ExceptionEvent<IllegalStateException> event)
    {
        protectedHandlerCalled = true;

        if (!event.isMarkedHandled())
        {
            event.handledAndContinue();
        }
    }

    @SuppressWarnings("unused")
    private void handlerLocationInjections(BeanManager bm, @Handles ExceptionEvent<SQLException> event)
    {
        if (bm != null)
        {
            locationDifferBeanmanagerInjected = true;
        }
    }

    public boolean isOutboundHandlerCalled()
    {
        return outboundHandlerCalled;
    }

    public int getOutboundHandlerTimesCalled()
    {
        return outboundHandlerTimesCalled;
    }

    public boolean isProtectedHandlerCalled()
    {
        return protectedHandlerCalled;
    }

    public void setOutboundHandlerTimesCalled(int outboundHandlerTimesCalled)
    {
        this.outboundHandlerTimesCalled = outboundHandlerTimesCalled;
    }

    public void setInboundHandlerTimesCalled(int inboundHandlerTimesCalled)
    {
        this.inboundHandlerTimesCalled = inboundHandlerTimesCalled;
    }

    public int getInboundHandlerTimesCalled()
    {
        return inboundHandlerTimesCalled;
    }

    public boolean isBeanmanagerInjected()
    {
        return beanmanagerInjected;
    }

    public boolean isLocationDifferBeanmanagerInjected()
    {
        return locationDifferBeanmanagerInjected;
    }
}
