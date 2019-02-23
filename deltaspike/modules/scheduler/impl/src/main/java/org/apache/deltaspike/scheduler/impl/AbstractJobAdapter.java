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

import org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.scheduler.spi.SchedulerControl;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.logging.Logger;

public abstract class AbstractJobAdapter<T> implements Job
{
    private static final Logger LOG = Logger.getLogger(AbstractJobAdapter.class.getName());

    @Inject
    private BeanManager beanManager;

    @Override
    public void execute(JobExecutionContext context)
    {
        Class<? extends T> jobClass =
                ClassUtils.tryToLoadClassForName(context.getJobDetail().getKey().getName(), getJobBaseClass());

        SchedulerControl schedulerControl = BeanProvider.getContextualReference(SchedulerControl.class, true);
        if (schedulerControl != null && schedulerControl.vetoJobExecution(jobClass))
        {
            LOG.info("Execution of job " + jobClass + " has been vetoed by " +
                    ProxyUtils.getUnproxiedClass(schedulerControl.getClass()));
            return;
        }

        T job = BeanProvider.getContextualReference(jobClass);

        try
        {
            execute(job, context);
        }
        catch (Throwable t)
        {
            //just in this case to reduce the implementation(s) of runnable (annotated with @Scheduled)
            //to an absolute minimum.
            //(custom implementations of org.quartz.Job need to do it on their own)
            this.beanManager.fireEvent(new ExceptionToCatchEvent(t));
        }
    }

    protected abstract Class<T> getJobBaseClass();

    public abstract void execute(T job, JobExecutionContext context) throws JobExecutionException;
}
