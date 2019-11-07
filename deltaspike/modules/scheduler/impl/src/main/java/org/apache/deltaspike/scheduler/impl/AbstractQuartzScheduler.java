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
import org.apache.deltaspike.core.api.provider.DependentProvider;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.PropertyFileUtils;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractQuartzScheduler<T> implements Scheduler<T>
{
    private static final Logger LOG = Logger.getLogger(AbstractQuartzScheduler.class.getName());
    private static final Scheduled DEFAULT_SCHEDULED_LITERAL = AnnotationInstanceProvider.of(Scheduled.class);

    private static ThreadLocal<JobListenerContext> currentJobListenerContext = new ThreadLocal<JobListenerContext>();

    protected org.quartz.Scheduler scheduler;

    @Override
    public void start()
    {
        if (this.scheduler != null)
        {
            throw new UnsupportedOperationException("the scheduler is started already");
        }

        SchedulerFactory schedulerFactory = null;
        try
        {
            Properties properties = new Properties();
            properties.put(StdSchedulerFactory.PROP_SCHED_JOB_FACTORY_CLASS, CdiAwareJobFactory.class.getName());

            try
            {
                ResourceBundle config = loadCustomQuartzConfig();

                Enumeration<String> keys = config.getKeys();
                String key;
                while (keys.hasMoreElements())
                {
                    key = keys.nextElement();
                    properties.put(key, config.getString(key));
                }
            }
            catch (Exception e1)
            {
                LOG.info("no custom quartz-config file found. falling back to the default config provided by quartz.");

                InputStream inputStream = null;
                try
                {
                    inputStream = ClassUtils.getClassLoader(null).getResourceAsStream("org/quartz/quartz.properties");
                    properties.load(inputStream);
                }
                catch (Exception e2)
                {
                    LOG.warning("failed to load quartz default-config");
                    schedulerFactory = new StdSchedulerFactory();
                }
                finally
                {
                    if (inputStream != null)
                    {
                        inputStream.close();
                    }
                }
            }
            if (schedulerFactory == null)
            {
                schedulerFactory = new StdSchedulerFactory(properties);
            }
        }
        catch (Exception e)
        {
            LOG.log(Level.WARNING, "fallback to default scheduler-factory", e);
            schedulerFactory = new StdSchedulerFactory();
        }

        try
        {
            this.scheduler = schedulerFactory.getScheduler();
            if (SchedulerBaseConfig.LifecycleIntegration.START_SCOPES_PER_JOB)
            {
                this.scheduler.getListenerManager().addJobListener(new InjectionAwareJobListener());
            }
            if (!this.scheduler.isStarted())
            {
                this.scheduler.startDelayed(SchedulerBaseConfig.LifecycleIntegration.DELAYED_START_IN_SECONDS);
            }
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    protected ResourceBundle loadCustomQuartzConfig()
    {
        //don't use quartz.properties as default-value
        String configFile = SchedulerBaseConfig.SCHEDULER_CONFIG_FILE;
        return PropertyFileUtils.getResourceBundle(configFile);
    }

    @Override
    public void stop()
    {
        try
        {
            if (this.scheduler != null && this.scheduler.isStarted())
            {
                this.scheduler.shutdown(!SchedulerBaseConfig.LifecycleIntegration.FORCE_STOP);
                this.scheduler = null;
            }
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public void registerNewJob(Class<? extends T> jobClass)
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

            JobDetail jobDetail = this.scheduler.getJobDetail(jobKey);
            Trigger trigger;

            if (jobDetail == null)
            {
                Class<? extends Job> jobClassToAdd = createFinalJobClass(jobClass);
                jobDetail = JobBuilder.newJob(jobClassToAdd)
                        .withDescription(description)
                        .withIdentity(jobKey)
                        .build();

                scheduleNewJob(scheduled, jobKey, jobDetail);
            }
            else if (scheduled.overrideOnStartup())
            {
                List<? extends Trigger> existingTriggers = this.scheduler.getTriggersOfJob(jobKey);

                if (existingTriggers == null || existingTriggers.isEmpty())
                {
                    scheduleNewJob(scheduled, jobKey, jobDetail);
                    return;
                }

                if (existingTriggers.size() > 1)
                {
                    throw new IllegalStateException("multiple triggers found for " + jobKey + " ('" + jobDetail + "')" +
                        ", but aren't supported by @" + Scheduled.class.getName() + "#overrideOnStartup");
                }

                trigger = existingTriggers.iterator().next();

                if (scheduled.cronExpression().startsWith("{") && scheduled.cronExpression().endsWith("}"))
                {
                    this.scheduler.unscheduleJobs(Arrays.asList(trigger.getKey()));

                    scheduleNewJob(scheduled, jobKey, jobDetail);
                }
                else
                {
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(trigger.getKey())
                            .withSchedule(CronScheduleBuilder.cronSchedule(scheduled.cronExpression()))
                            .build();

                    this.scheduler.rescheduleJob(trigger.getKey(), trigger);
                }
            }
            else
            {
                Logger.getLogger(AbstractQuartzScheduler.class.getName()).info(
                    jobKey + " exists already and will be ignored.");
            }
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    private void scheduleNewJob(Scheduled scheduled, JobKey jobKey, JobDetail jobDetail) throws SchedulerException
    {
        String cronExpression = evaluateExpression(scheduled);
        this.scheduler.scheduleJob(jobDetail, createTrigger(scheduled, jobKey, cronExpression));
    }

    private Trigger createTrigger(Scheduled scheduled, JobKey jobKey, String cronExpression) throws SchedulerException
    {
        UUID triggerKey = UUID.randomUUID();

        if (!scheduled.cronExpression().endsWith(cronExpression))
        {
            createExpressionObserverJob(jobKey, triggerKey, scheduled.cronExpression(), cronExpression);
        }

        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobKey)
                .withIdentity(triggerKey.toString())
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
        return trigger;
    }

    private void createExpressionObserverJob(
        JobKey jobKey, UUID triggerKey, String configExpression, String cronExpression) throws SchedulerException
    {
        if (!ClassDeactivationUtils.isActivated(DynamicExpressionObserverJob.class))
        {
            return;
        }

        JobKey observerJobKey =
                new JobKey(jobKey.getName() + DynamicExpressionObserverJob.OBSERVER_POSTFIX, jobKey.getGroup());

        JobDetail jobDetail  = JobBuilder.newJob(DynamicExpressionObserverJob.class)
                .usingJobData(DynamicExpressionObserverJob.CONFIG_EXPRESSION_KEY, configExpression)
                .usingJobData(DynamicExpressionObserverJob.TRIGGER_ID_KEY, triggerKey.toString())
                .usingJobData(DynamicExpressionObserverJob.ACTIVE_CRON_EXPRESSION_KEY, cronExpression)
                .withDescription("Config observer for: " + jobKey)
                .withIdentity(observerJobKey)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(observerJobKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(
                    SchedulerBaseConfig.JobCustomization.DYNAMIC_EXPRESSION_OBSERVER_INTERVAL))
                .build();

        this.scheduler.scheduleJob(jobDetail, trigger);
    }

    private String evaluateExpression(Scheduled scheduled)
    {
        String expression = scheduled.cronExpression();

        if (expression.startsWith("{") && expression.endsWith("}"))
        {
            String configKey = expression.substring(1, expression.length() - 1);
            expression = ConfigResolver.getProjectStageAwarePropertyValue(configKey, null);

            if (expression == null)
            {
                throw new IllegalStateException("No config-value found for config-key: " + configKey);
            }
        }
        return expression;
    }

    protected abstract Class<? extends Job> createFinalJobClass(Class<? extends T> jobClass);

    @Override
    public void startJobManually(Class<? extends T> jobClass)
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
    public void interruptJob(Class<? extends T> jobClass)
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
    public boolean deleteJob(Class<? extends T> jobClass)
    {
        try
        {
            return this.scheduler.deleteJob(createJobKey(jobClass));
        }
        catch (SchedulerException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public void pauseJob(Class<? extends T> jobClass)
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
    public void resumeJob(Class<? extends T> jobClass)
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
    public boolean isExecutingJob(Class<? extends T> jobClass)
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

    private JobKey createJobKey(Class<?> jobClass)
    {
        Scheduled scheduled = jobClass.getAnnotation(Scheduled.class);

        if (scheduled == null)
        {
            throw new IllegalStateException("@" + Scheduled.class.getName() + " is missing on " + jobClass.getName());
        }

        String groupName = scheduled.group().getSimpleName();
        String jobName = getJobName(jobClass);

        if (!Scheduled.class.getSimpleName().equals(groupName))
        {
            return new JobKey(jobName, groupName);
        }
        return new JobKey(jobName);
    }

    protected String getJobName(Class<?> jobClass)
    {
        return jobClass.getSimpleName();
    }

    private class InjectionAwareJobListener implements JobListener
    {
        @Override
        public String getName()
        {
            return getClass().getName();
        }

        @Override
        public void jobToBeExecuted(JobExecutionContext jobExecutionContext)
        {
            Class<?> jobClass = ProxyUtils.getUnproxiedClass(jobExecutionContext.getJobInstance().getClass());
            Scheduled scheduled = jobClass.getAnnotation(Scheduled.class);

            //can happen with manually registered job-instances (via #unwrap)
            if (scheduled == null && !jobClass.equals(DynamicExpressionObserverJob.class))
            {
                scheduled = DEFAULT_SCHEDULED_LITERAL;
            }

            if (scheduled == null)
            {
                return;
            }

            JobListenerContext jobListenerContext = new JobListenerContext();
            currentJobListenerContext.set(jobListenerContext);
            jobListenerContext.startContexts(scheduled);

            boolean jobInstanceIsBean;

            try
            {
                jobInstanceIsBean =
                    Boolean.TRUE.equals(jobExecutionContext.getScheduler().getContext().get(jobClass.getName()));
            }
            catch (SchedulerException e)
            {
                jobInstanceIsBean = false;
            }

            if (!jobInstanceIsBean)
            {
                BeanProvider.injectFields(jobExecutionContext.getJobInstance());
            }
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
            JobListenerContext jobListenerContext = currentJobListenerContext.get();
            if (jobListenerContext != null)
            {
                jobListenerContext.stopStartedScopes();
                currentJobListenerContext.set(null);
                currentJobListenerContext.remove();
            }
        }
    }

    private static class JobListenerContext
    {
        private Stack<Class<? extends Annotation>> scopes = new Stack<Class<? extends Annotation>>();
        private DependentProvider<ContextControl> contextControl;

        public void startContexts(Scheduled scheduled)
        {
            Collections.addAll(this.scopes, scheduled.startScopes());

            if (!this.scopes.isEmpty())
            {
                this.contextControl = BeanProvider.getDependent(ContextControl.class);

                for (Class<? extends Annotation> scopeAnnotation : this.scopes)
                {
                    contextControl.get().startContext(scopeAnnotation);
                }
            }
        }

        private void stopStartedScopes()
        {
            if (this.contextControl == null)
            {
                return;
            }

            while (!this.scopes.empty())
            {
                this.contextControl.get().stopContext(this.scopes.pop());
            }
            this.contextControl.destroy();
        }
    }

    @Override
    public <S> S unwrap(Class<? extends S> schedulerClass)
    {
        if (schedulerClass.isAssignableFrom(this.scheduler.getClass()))
        {
            return (S)this.scheduler;
        }

        throw new IllegalArgumentException(schedulerClass.getName() +
            " isn't compatible with " + this.scheduler.getClass().getName());
    }
}
