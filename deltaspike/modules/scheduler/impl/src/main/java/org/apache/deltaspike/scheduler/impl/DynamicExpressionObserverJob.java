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

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.scheduler.spi.Scheduler;
import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * This job is only active, if configurable cron-expressions are used - e.g.: @Scheduled(cronExpression = "{myKey}").
 * It observes jobs with configurable cron-expressions and updates their job-triggers once a config-change was detected.
 * Per default this job gets executed once per minute. That can be changed via config-entry:
 * deltaspike.scheduler.dynamic-expression.observer-interval=[any valid cron-expression]
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class DynamicExpressionObserverJob implements Deactivatable, Job
{
    static final String CONFIG_EXPRESSION_KEY = "ds_configExpression";
    static final String ACTIVE_CRON_EXPRESSION_KEY = "ds_activeCronExpression";
    static final String TRIGGER_ID_KEY = "ds_triggerKey";
    static final String OBSERVER_POSTFIX = "_observer";

    private static final Logger LOG = Logger.getLogger(DynamicExpressionObserverJob.class.getName());

    @Inject
    private Scheduler<Job> scheduler;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String configExpression = jobDataMap.getString(CONFIG_EXPRESSION_KEY);
        String triggerId = jobDataMap.getString(TRIGGER_ID_KEY);
        String activeCronExpression = jobDataMap.getString(ACTIVE_CRON_EXPRESSION_KEY);

        String configKey = configExpression.substring(1, configExpression.length() - 1);
        String configuredValue = ConfigResolver.getPropertyAwarePropertyValue(configKey, activeCronExpression);

        if (!activeCronExpression.equals(configuredValue))
        {
            //both #put calls are needed currently
            context.getJobDetail().getJobDataMap().put(ACTIVE_CRON_EXPRESSION_KEY, configuredValue);
            context.getTrigger().getJobDataMap().put(ACTIVE_CRON_EXPRESSION_KEY, configuredValue);

            BeanProvider.injectFields(this);

            JobKey observerJobKey = context.getJobDetail().getKey();
            String observedJobName = observerJobKey.getName()
                .substring(0, observerJobKey.getName().length() - OBSERVER_POSTFIX.length());
            JobKey observedJobKey = new JobKey(observedJobName, observerJobKey.getGroup());

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerId)
                    .forJob(observedJobName, observedJobKey.getGroup())
                    .withSchedule(CronScheduleBuilder.cronSchedule(configuredValue))
                    .build();

            //use rescheduleJob instead of delete + add
            //(unwrap is ok here, because this class will only get active in case of a quartz-scheduler)
            org.quartz.Scheduler quartzScheduler = scheduler.unwrap(org.quartz.Scheduler.class);
            try
            {
                quartzScheduler.rescheduleJob(trigger.getKey(), trigger);
            }
            catch (SchedulerException e)
            {
                LOG.warning("failed to updated cron-expression for " + observedJobKey);
            }
        }
    }
}
