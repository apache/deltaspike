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
import org.apache.deltaspike.test.core.impl.exception.control.extension.Account;
import org.apache.deltaspike.test.core.impl.exception.control.extension.Arquillian;
import org.apache.deltaspike.test.core.impl.exception.control.extension.CatchQualifier;

import javax.enterprise.inject.spi.BeanManager;
import java.sql.SQLException;

@ExceptionHandler
public class ExtensionExceptionHandler
{
    public void catchDescException(@BeforeHandles ExceptionEvent<Exception> event)
    {
        // Nothing to do currently
    }

    public void catchFrameworkDescException(@BeforeHandles(ordinal = -50) ExceptionEvent<Exception> event)
    {
        // Nothing to do here
    }

    public void catchRuntime(@Handles ExceptionEvent<RuntimeException> event)
    {
        // Nothing to do currently
    }

    public void catchThrowableBreadthFirst(
            @BeforeHandles(ordinal = 10) ExceptionEvent<Throwable> event)
    {
        // Nothing to do currently
    }

    public void catchThrowableP20BreadthFirst(
            @BeforeHandles(ordinal = 20) ExceptionEvent<Throwable> event)
    {
        // Nothing to do currently
    }

    public void catchThrowable(
            @Handles(ordinal = 10) ExceptionEvent<Throwable> event)
    {
        // Nothing to do currently
    }

    public void catchThrowableP20(
            @Handles(ordinal = 20) ExceptionEvent<Throwable> event)
    {
        // Nothing to do currently
    }

    public void catchIAE(@Handles ExceptionEvent<IllegalArgumentException> event)
    {
        // Nothing to do currently
    }

    public void qualifiedHandler(@Handles @CatchQualifier ExceptionEvent<Exception> event)
    {
        // Method to verify the qualifiers are working correctly for handlers
    }

    public void arqHandler(@Handles @Arquillian ExceptionEvent<Throwable> event)
    {
        // Method to verify the qualifiers are working correctly for handlers
    }

    public void arqTestingHandler(@Handles @Arquillian @CatchQualifier ExceptionEvent<Throwable> event)
    {
        // Method to verify the qualifiers are working correctly for handlers
    }

    public void differentParamHandlerLocationHandler(Account act, BeanManager bm,
                                                     @Handles ExceptionEvent<SQLException> event)
    {
        // Nothing here, just need to make sure this handler is picked up
    }

    public void npeHandlerNoDefQualifier(@Handles ExceptionEvent<NullPointerException> event)
    {

    }

    public void npeHandlerDefQualifier(@Handles @CatchQualifier ExceptionEvent<NullPointerException> event)
    {

    }

    public void doNothingMethod()
    {
        // Method to make sure only @Handles methods are found
    }

    public void doNothingTwo(String p1, String p2, int p3)
    {
        // Same as above
    }
}
