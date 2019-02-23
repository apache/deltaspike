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
package org.apache.deltaspike.scheduler.spi;

/**
 * This interface provides high-level controls for the scheduler.
 *
 * It allows to control the scheduler as a whole ({@link #isSchedulerEnabled()}()) and on a per-job basis
 * ({@link #vetoJobExecution(Class)}.
 *
 * The interface is meant to be implemented by a CDI bean.
 */
public interface SchedulerControl
{
    /**
     * Control whether or not the scheduler should be started.
     *
     * @return if {@code true} the scheduler will be started, else not.
     */
    default boolean isSchedulerEnabled()
    {
        return true;
    }

    /**
     * Invoked each time a job is triggered, this controls whether the given job shall be started or not.
     *
     * NOTE: This only applies if the scheduler is actually running (see {@link #isSchedulerEnabled()}).
     *
     *  @param jobClass the job which was triggered
     * @return if {@code false} the job will be executed, else not.
     */
    default boolean vetoJobExecution(Class<?> jobClass)
    {
        return false;
    }
}
