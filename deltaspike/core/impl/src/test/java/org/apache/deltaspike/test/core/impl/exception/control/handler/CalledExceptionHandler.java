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
import org.apache.deltaspike.core.api.exception.control.CaughtException;
import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.Handles;

import javax.enterprise.inject.spi.BeanManager;
import java.sql.SQLException;

@ExceptionHandler
public class CalledExceptionHandler
{
    public static boolean OUTBOUND_HANDLER_CALLED = false;
    public static int OUTBOUND_HANDLER_TIMES_CALLED = 0;
    public static boolean PROTECTED_HANDLER_CALLED = false;
    public static int INBOUND_HANDLER_TIMES_CALLED = 0;
    public static boolean BEANMANAGER_INJECTED = false;
    public static boolean LOCATION_DIFFER_BEANMANAGER_INJECTED = false;

    public void basicHandler(@Handles CaughtException<Exception> event)
    {
        OUTBOUND_HANDLER_CALLED = true;
        OUTBOUND_HANDLER_TIMES_CALLED++;
    }

    public void basicInboundHandler(@BeforeHandles CaughtException<Exception> event)
    {
        INBOUND_HANDLER_TIMES_CALLED++;
        event.handledAndContinue();
    }

    public void extraInjections(@Handles CaughtException<IllegalArgumentException> event, BeanManager bm)
    {
        if (bm != null)
        {
            BEANMANAGER_INJECTED = true;
        }
    }

    void protectedHandler(@Handles CaughtException<IllegalStateException> event)
    {
        PROTECTED_HANDLER_CALLED = true;

        if (!event.isMarkedHandled())
        {
            event.handledAndContinue();
        }
    }

    @SuppressWarnings("unused")
    private void handlerLocationInjections(BeanManager bm, @Handles CaughtException<SQLException> event)
    {
        if (bm != null)
        {
            LOCATION_DIFFER_BEANMANAGER_INJECTED = true;
        }
    }
}
