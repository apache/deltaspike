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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import java.util.ArrayList;
import java.util.List;

@Typed()
public class TestJobManager
{
    private static TestJobManager currentManager = new TestJobManager();

    private boolean started;

    private List<Class<? extends CustomJob>> registeredJobs = new ArrayList<Class<? extends CustomJob>>();
    private List<Class<? extends CustomJob>> runningJobs = new ArrayList<Class<? extends CustomJob>>();

    public static TestJobManager getInstance()
    {
        return currentManager;
    }

    @Produces
    @ApplicationScoped
    protected TestJobManager expose()
    {
        return currentManager;
    }

    public void start()
    {
        this.started = true;
    }

    public void stop()
    {
        currentManager = new TestJobManager();
    }

    public void pauseJob(Class<? extends CustomJob> jobClass)
    {
        this.registeredJobs.remove(jobClass);
    }

    public void resumeJob(Class<? extends CustomJob> jobClass)
    {
        this.registeredJobs.add(jobClass);
    }

    public void interruptJob(Class<? extends CustomJob> jobClass)
    {
        this.runningJobs.remove(jobClass);
    }

    public boolean deleteJob(Class<? extends CustomJob> jobClass)
    {
        return this.registeredJobs.remove(jobClass);
    }

    public boolean isExecutingJob(Class<? extends CustomJob> jobClass)
    {
        return this.runningJobs.contains(jobClass);
    }

    public void registerNewJob(Class<? extends CustomJob> jobClass)
    {
        this.registeredJobs.add(jobClass);
        this.runningJobs.add(jobClass);
    }

    public void startJobManually(Class<? extends CustomJob> jobClass)
    {
        this.runningJobs.add(jobClass);
    }

    public boolean isStarted()
    {
        return started;
    }

    public List<Class<? extends CustomJob>> getRegisteredJobs()
    {
        return registeredJobs;
    }

    public List<Class<? extends CustomJob>> getRunningJobs()
    {
        return runningJobs;
    }
}
