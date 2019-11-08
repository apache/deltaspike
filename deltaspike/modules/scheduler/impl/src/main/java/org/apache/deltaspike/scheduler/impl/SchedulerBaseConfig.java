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
import org.apache.deltaspike.core.api.config.base.DeltaSpikeBaseConfig;

//keep it in the impl. module for now, because it's mainly quartz specific config
public interface SchedulerBaseConfig extends DeltaSpikeBaseConfig
{
    interface JobCustomization
    {
        String JOB_CLASS_NAME_KEY = "deltaspike.scheduler.job-class";
        String RUNNABLE_ADAPTER_CLASS_NAME_KEY = "deltaspike.scheduler.runnable-adapter-class";
        String DYNAMIC_EXPRESSION_OBSERVER_INTERVAL_KEY =
            "deltaspike.scheduler.dynamic-expression.observer-interval";

        //don't type it to class to keep quartz optional
        String DEFAULT_JOB_FACTORY_CLASS_NAME = ConfigResolver.resolve("deltaspike.scheduler.DefaultJobFactory")
                .withCurrentProjectStage(true)
                .withDefault("org.quartz.simpl.PropertySettingJobFactory")
                .getValue();

        //don't type it to class to keep quartz optional
        String JOB_CLASS_NAME = ConfigResolver.resolve(JOB_CLASS_NAME_KEY)
                .withCurrentProjectStage(true)
                .withDefault("org.quartz.Job")
                .getValue();

        //don't type it to class to keep quartz optional (JobRunnableAdapter imports classes from quartz)
        String RUNNABLE_ADAPTER_CLASS_NAME = ConfigResolver.resolve(RUNNABLE_ADAPTER_CLASS_NAME_KEY)
                .withCurrentProjectStage(true)
                .withDefault("org.apache.deltaspike.scheduler.impl.JobRunnableAdapter")
                .getValue();

        String DYNAMIC_EXPRESSION_OBSERVER_INTERVAL =
                ConfigResolver.resolve(DYNAMIC_EXPRESSION_OBSERVER_INTERVAL_KEY)
                .withCurrentProjectStage(true)
                .withDefault("0 0/1 * * * ?")
                .getValue();
    }

    String SCHEDULER_CONFIG_FILE = ConfigResolver.resolve("deltaspike.scheduler.quartz_config-file")
            .withCurrentProjectStage(true)
            .withDefault("quartz")
            .getValue();

    interface LifecycleIntegration
    {
        String START_SCOPES_PER_JOB_KEY = "deltaspike.scheduler.start_scopes_for_jobs";

        Boolean START_SCOPES_PER_JOB = ConfigResolver.resolve(START_SCOPES_PER_JOB_KEY)
                .as(Boolean.class)
                .withCurrentProjectStage(true)
                .withDefault(Boolean.TRUE)
                .getValue();

        Boolean FORCE_STOP = ConfigResolver.resolve("deltaspike.scheduler.force_stop")
                .as(Boolean.class)
                .withCurrentProjectStage(true)
                .withDefault(Boolean.FALSE)
                .getValue();

        Integer DELAYED_START_IN_SECONDS = ConfigResolver.resolve("deltaspike.scheduler.delayed_start_in_seconds")
                .as(Integer.class)
                .withCurrentProjectStage(true)
                .withDefault(1)
                .getValue();
    }
}
