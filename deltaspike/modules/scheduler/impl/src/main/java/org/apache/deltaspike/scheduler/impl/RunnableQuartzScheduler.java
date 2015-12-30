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
package org.apache.deltaspike.scheduler.impl;

import org.apache.deltaspike.core.util.ClassUtils;
import org.quartz.Job;

//vetoed class (see SchedulerExtension)
public class RunnableQuartzScheduler extends AbstractQuartzScheduler<Runnable>
{
    private Class<? extends Job> runnableAdapter;

    @Override
    public void start()
    {
        String configuredAdapterClassName = SchedulerBaseConfig.JobCustomization.RUNNABLE_ADAPTER_CLASS_NAME;
        this.runnableAdapter = ClassUtils.tryToLoadClassForName(configuredAdapterClassName, Job.class);

        super.start();
    }

    @Override
    protected String getJobName(Class<?> jobClass)
    {
        return jobClass.getName();
    }

    @Override
    protected Class<? extends Job> createFinalJobClass(Class<? extends Runnable> jobClass)
    {
        return runnableAdapter;
    }
}
