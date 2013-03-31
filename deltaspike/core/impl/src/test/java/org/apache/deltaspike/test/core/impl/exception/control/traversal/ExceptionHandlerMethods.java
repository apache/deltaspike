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

package org.apache.deltaspike.test.core.impl.exception.control.traversal;

import org.apache.deltaspike.core.api.exception.control.BeforeHandles;
import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.Handles;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;

import java.util.ArrayList;
import java.util.List;

@ExceptionHandler
public class ExceptionHandlerMethods
{
    private static final List<Integer> executionOrder = new ArrayList<Integer>();

    public void handleException1BF(@BeforeHandles ExceptionEvent<Exceptions.Exception1> event)
    {
        executionOrder.add(7);
    }

    public void handleException2BF(@BeforeHandles ExceptionEvent<Exceptions.Exception2> event)
    {
        executionOrder.add(5);
    }

    public void handleException3DF(@Handles ExceptionEvent<Exceptions.Exception3> event)
    {
        executionOrder.add(3);
    }

    public void handleException3BF(@BeforeHandles ExceptionEvent<Exceptions.Exception3> event)
    {
        executionOrder.add(2);
    }

    public void handleException3SuperclassBF(@BeforeHandles ExceptionEvent<Exceptions.Exception3Super> event)
    {
        executionOrder.add(1);
    }

    public void handleException3SuperclassDF(@Handles ExceptionEvent<Exceptions.Exception3Super> event)
    {
        executionOrder.add(4);
    }

    public void handleException2DF(@Handles ExceptionEvent<Exceptions.Exception2> event)
    {
        executionOrder.add(6);
    }

    public void handleException1DF(@Handles ExceptionEvent<Exceptions.Exception1> event)
    {
        executionOrder.add(8);
    }

    public static List<Integer> getExecutionorder()
    {
        return executionOrder;
    }
}
