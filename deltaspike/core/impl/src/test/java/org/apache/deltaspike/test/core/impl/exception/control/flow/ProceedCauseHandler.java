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

@ApplicationScoped
@ExceptionHandler
public class ProceedCauseHandler
{
    private int breadthFirstNpeCalled = 0;
    private int breadthFirstNpeLowerPrecedenceCalled = 0;

    private int depthFirstNpeCalled = 0;
    private int depthFirstNpeHigherPrecedenceCalled = 0;

    public void npeInboundHandler(@BeforeHandles ExceptionEvent<NullPointerException> event)
    {
        breadthFirstNpeCalled++;
        event.skipCause();
    }

    public void npeLowerPrecedenceInboundHandler(
            @BeforeHandles(ordinal = -50) ExceptionEvent<NullPointerException> event)
    {
        breadthFirstNpeLowerPrecedenceCalled++;
        event.handledAndContinue();
    }

    public void npeOutboundHandler(@Handles ExceptionEvent<NullPointerException> event)
    {
        depthFirstNpeCalled++;
        event.skipCause();
    }

    public void npeHigherPrecedenceOutboundHandler(@Handles(ordinal = -10) ExceptionEvent<NullPointerException> event)
    {
        depthFirstNpeHigherPrecedenceCalled++;
        event.handledAndContinue();
    }

    public int getBreadthFirstNpeCalled()
    {
        return breadthFirstNpeCalled;
    }

    public int getBreadthFirstNpeLowerPrecedenceCalled()
    {
        return breadthFirstNpeLowerPrecedenceCalled;
    }

    public int getDepthFirstNpeCalled()
    {
        return depthFirstNpeCalled;
    }

    public int getDepthFirstNpeHigherPrecedenceCalled()
    {
        return depthFirstNpeHigherPrecedenceCalled;
    }
}
