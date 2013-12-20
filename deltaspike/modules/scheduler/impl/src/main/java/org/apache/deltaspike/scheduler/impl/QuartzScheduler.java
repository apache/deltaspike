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

import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.scheduler.api.Scheduled;
import org.apache.deltaspike.scheduler.spi.Scheduler;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Stack;

//vetoed class (see SchedulerExtension)
public class QuartzScheduler implements Scheduler<Job>
{
    private org.quartz.Scheduler scheduler;

    @Override
    public void start()
    {
        if (this.scheduler != null)
        {
            throw new UnsupportedOperationException("the scheduler is started already");
        }

        SchedulerFactory schedulerFactory;
        try
        {
            String configFile =
                ConfigResolver.getPropertyValue("deltaspike.scheduler.quartz_config-file", "quartz.properties");
            schedulerFactory = new StdSchedulerFactory(configFile);
        }
        catch (SchedulerException e)
        {
            schedulerFactory = new StdSchedulerFactory();
        }

        try
        {
            this.scheduler = schedulerFactory.getScheduler();
            this.scheduler.getListenerManager().addJobListener(new InjectionAwareJobListener());

            if (!this.scheduler.isStarted())
            {
                String delayedStart =
                    ConfigResolver.getPropertyValue("deltaspike.scheduler.delayed_start_in_seconds", "1");
                this.scheduler.startDelayed(Integer.parseInt(delayedStart));
            }
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public void stop()
    {
        try
        {
            if (this.scheduler != null && this.scheduler.isStarted())
            {
                String forceStop = ConfigResolver.getPropertyValue("deltaspike.scheduler.force_stop", "true");

                this.scheduler.shutdown(Boolean.parseBoolean(forceStop));
                this.scheduler = null;
            }
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public void registerNewJob(Class<? extends Job> jobClass)
    {
        JobKey jobKey = createJobKey(jobClass);

        try
        {
            Scheduled scheduled = jobClass.getAnnotation(Scheduled.class);

            String description = scheduled.description();

            if ("".equals(scheduled.description()))
            {
                description = jobClass.getName();
            }

            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withDescription(description)
                    .withIdentity(jobKey)
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.cronSchedule(scheduled.cronExpression()))
                    .build();

            this.scheduler.scheduleJob(jobDetail, trigger);
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public void startJobManually(Class<? extends Job> jobClass)
    {
        try
        {
            this.scheduler.triggerJob(createJobKey(jobClass));
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public void interruptJob(Class<? extends Job> jobClass)
    {
        try
        {
            this.scheduler.interrupt(createJobKey(jobClass));
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public void pauseJob(Class<? extends Job> jobClass)
    {
        try
        {
            this.scheduler.pauseJob(createJobKey(jobClass));
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public void resumeJob(Class<? extends Job> jobClass)
    {
        try
        {
            this.scheduler.resumeJob(createJobKey(jobClass));
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public boolean isExecutingJob(Class<? extends Job> jobClass)
    {
        try
        {
            JobKey jobKey = createJobKey(jobClass);
            JobDetail jobDetail = this.scheduler.getJobDetail(jobKey);

            if (jobDetail == null)
            {
                return false;
            }

            for (JobExecutionContext jobExecutionContext : this.scheduler.getCurrentlyExecutingJobs())
            {
                if (jobKey.equals(jobExecutionContext.getJobDetail().getKey()))
                {
                    return true;
                }
            }

            return false;
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    private static JobKey createJobKey(Class<?> jobClass)
    {
        Scheduled scheduled = jobClass.getAnnotation(Scheduled.class);

        if (scheduled == null)
        {
            throw new IllegalStateException("@" + Scheduled.class.getName() + " is missing on " + jobClass.getName());
        }

        String groupName = scheduled.group().getSimpleName();
        String jobName = jobClass.getSimpleName();

        if (!Scheduled.class.getSimpleName().equals(groupName))
        {
            return new JobKey(jobName, groupName);
        }
        return new JobKey(jobName);
    }

    private class InjectionAwareJobListener implements JobListener
    {
        private Stack<Class<? extends Annotation>> scopes = new Stack<Class<? extends Annotation>>();
        private ContextControl contextControl;

        @Override
        public String getName()
        {
            return getClass().getName();
        }

        @Override
        public void jobToBeExecuted(JobExecutionContext jobExecutionContext)
        {
            Scheduled scheduled = jobExecutionContext.getJobInstance().getClass().getAnnotation(Scheduled.class);

            Collections.addAll(this.scopes, scheduled.startScopes());

            if (!this.scopes.isEmpty())
            {
                this.contextControl = BeanProvider.getContextualReference(ContextControl.class);

                for (Class<? extends Annotation> scopeAnnotation : this.scopes)
                {
                    contextControl.startContext(scopeAnnotation);
                }
            }

            BeanProvider.injectFields(jobExecutionContext.getJobInstance());
        }

        @Override
        public void jobExecutionVetoed(JobExecutionContext context)
        {
            stopStartedScopes();
        }

        @Override
        public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException)
        {
            stopStartedScopes();
        }

        private void stopStartedScopes()
        {
            while (!this.scopes.empty())
            {
                this.contextControl.stopContext(this.scopes.pop());
            }
        }
    }
}
