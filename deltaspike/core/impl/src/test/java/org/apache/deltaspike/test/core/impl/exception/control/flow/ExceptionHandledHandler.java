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

package org.apache.deltaspike.test.core.impl.exception.control.flow;

import org.apache.deltaspike.core.api.exception.control.BeforeHandles;
import org.apache.deltaspike.core.api.exception.control.CaughtException;
import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.Handles;

@ExceptionHandler
public class ExceptionHandledHandler
{
    public static boolean EX_ASC_CALLED = false;
    public static boolean IAE_ASC_CALLED = false;
    public static boolean NPE_DESC_CALLED = false;

    public void exHandler(@Handles CaughtException<Exception> event)
    {
        EX_ASC_CALLED = true;
    }

    public void npeHandler(@Handles CaughtException<IllegalArgumentException> event)
    {
        IAE_ASC_CALLED = true;
        event.handled();
    }

    public void npeDescHandler(@BeforeHandles CaughtException<NullPointerException> event)
    {
        NPE_DESC_CALLED = true;
        event.handled();
    }
}
