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
package org.apache.deltaspike.test.scheduler.custom;

import org.apache.deltaspike.scheduler.spi.Scheduler;

public class MockedScheduler implements Scheduler<CustomJob>
{
    @Override
    public void start()
    {
        TestJobManager.getInstance().start();
    }

    @Override
    public void stop()
    {
        TestJobManager.getInstance().stop();
    }

    public void pauseJob(Class<? extends CustomJob> jobClass)
    {
        TestJobManager.getInstance().pauseJob(jobClass);
    }

    public void resumeJob(Class<? extends CustomJob> jobClass)
    {
        TestJobManager.getInstance().resumeJob(jobClass);
    }

    public void interruptJob(Class<? extends CustomJob> jobClass)
    {
        TestJobManager.getInstance().interruptJob(jobClass);
    }

    public boolean deleteJob(Class<? extends CustomJob> jobClass) {
        return TestJobManager.getInstance().deleteJob(jobClass);
    }

    public boolean isExecutingJob(Class<? extends CustomJob> jobClass)
    {
        return TestJobManager.getInstance().isExecutingJob(jobClass);
    }

    public void registerNewJob(Class<? extends CustomJob> jobClass)
    {
        TestJobManager.getInstance().registerNewJob(jobClass);
    }

    public void startJobManually(Class<? extends CustomJob> jobClass)
    {
        TestJobManager.getInstance().startJobManually(jobClass);
    }

    @Override
    public <S> S unwrap(Class<? extends S> schedulerClass)
    {
        if (schedulerClass.isAssignableFrom(TestJobManager.getInstance().getClass()))
        {
            return (S)TestJobManager.getInstance();
        }

        throw new IllegalArgumentException(schedulerClass.getName() +
            " isn't compatible with " + TestJobManager.getInstance().getClass().getName());
    }
}
