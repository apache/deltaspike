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

import junit.framework.Assert;
import org.apache.deltaspike.scheduler.spi.Scheduler;
import org.junit.Test;

import javax.inject.Inject;

public abstract class CustomSchedulerTest
{
    @Inject
    private Scheduler scheduler;
    //workaround for weld-se - as an alternative it's possible to use:
    /*
    private Scheduler<CustomJob> scheduler;

    @Before
    public void init()
    {
        this.scheduler = BeanProvider.getContextualReference(Scheduler.class);
    }
    */

    @Inject
    private TestJobManager testJobManager;

    @Test
    public void checkAutoRegisteredSchedulerJob()
    {
        Assert.assertTrue(testJobManager.isStarted());
        Assert.assertEquals(1, testJobManager.getRegisteredJobs().size());
        Assert.assertEquals(AutoRegisteredJob.class, testJobManager.getRegisteredJobs().iterator().next());
        Assert.assertEquals(1, testJobManager.getRunningJobs().size());
        Assert.assertEquals(AutoRegisteredJob.class, testJobManager.getRunningJobs().iterator().next());
    }

    @Test
    public void checkManualSchedulerJobManagement()
    {
        Assert.assertTrue(testJobManager.isStarted());
        Assert.assertEquals(1, testJobManager.getRegisteredJobs().size());
        Assert.assertEquals(AutoRegisteredJob.class, testJobManager.getRegisteredJobs().iterator().next());
        Assert.assertEquals(1, testJobManager.getRunningJobs().size());
        Assert.assertEquals(AutoRegisteredJob.class, testJobManager.getRunningJobs().iterator().next());

        this.scheduler.registerNewJob(ManualJob.class);
        Assert.assertEquals(2, testJobManager.getRegisteredJobs().size());
        Assert.assertEquals(2, testJobManager.getRunningJobs().size());

        this.scheduler.interruptJob(AutoRegisteredJob.class);
        Assert.assertEquals(1, testJobManager.getRunningJobs().size());
        Assert.assertEquals(ManualJob.class, testJobManager.getRunningJobs().iterator().next());

        Assert.assertEquals(2, testJobManager.getRegisteredJobs().size());
        this.scheduler.pauseJob(AutoRegisteredJob.class);
        Assert.assertEquals(1, testJobManager.getRegisteredJobs().size());
        Assert.assertEquals(ManualJob.class, testJobManager.getRegisteredJobs().iterator().next());

        this.scheduler.pauseJob(ManualJob.class);
        Assert.assertEquals(0, testJobManager.getRegisteredJobs().size());
        this.scheduler.resumeJob(ManualJob.class);
        Assert.assertEquals(1, testJobManager.getRegisteredJobs().size());
        Assert.assertEquals(ManualJob.class, testJobManager.getRegisteredJobs().iterator().next());

        this.scheduler.pauseJob(ManualJob.class);
        this.scheduler.interruptJob(ManualJob.class);
        Assert.assertEquals(0, testJobManager.getRunningJobs().size());
        Assert.assertEquals(0, testJobManager.getRegisteredJobs().size());

        this.scheduler.startJobManually(ManualJob.class);
        Assert.assertTrue(this.scheduler.isExecutingJob(ManualJob.class));
        Assert.assertEquals(0, testJobManager.getRegisteredJobs().size());
        this.scheduler.interruptJob(ManualJob.class);

        this.scheduler.registerNewJob(AutoRegisteredJob.class);
        Assert.assertEquals(1, testJobManager.getRegisteredJobs().size());

        this.scheduler.deleteJob(AutoRegisteredJob.class);
        Assert.assertEquals(0, testJobManager.getRegisteredJobs().size());
    }

    @Test
    public void unwrap()
    {
        Assert.assertEquals(TestJobManager.class, this.scheduler.unwrap(TestJobManager.class).getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidUnwrap()
    {
        this.scheduler.unwrap(MockedScheduler.class);
    }
}
