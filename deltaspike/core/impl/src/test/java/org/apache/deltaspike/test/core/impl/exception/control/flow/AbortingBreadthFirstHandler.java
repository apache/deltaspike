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
import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.Handles;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;

import javax.enterprise.context.ApplicationScoped;

@SuppressWarnings({"AssignmentToStaticFieldFromInstanceMethod"})
@ApplicationScoped
@ExceptionHandler
public class AbortingBreadthFirstHandler
{
    private boolean abortCalled = false;
    private boolean proceedCalled = false;

    public void abortHandler(@BeforeHandles ExceptionEvent<Exception> event)
    {
        abortCalled = true;
        event.abort();
    }

    public void proceedHandler(@Handles ExceptionEvent<NullPointerException> event)
    {
        proceedCalled = true;
        event.handledAndContinue();
    }

    public boolean isAbortCalled()
    {
        return abortCalled;
    }

    public boolean isProceedCalled()
    {
        return proceedCalled;
    }
}
