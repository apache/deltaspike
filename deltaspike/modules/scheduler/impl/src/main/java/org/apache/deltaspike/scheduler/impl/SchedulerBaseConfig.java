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

import org.apache.deltaspike.core.api.config.base.TypedConfig;

//keep it in the impl. module for now, because it's mainly quartz specific config
public interface SchedulerBaseConfig
{
    interface Job
    {
        //don't type it to class to keep quartz optional
        TypedConfig<String> DEFAULT_JOB_FACTORY_CLASS_NAME =
            new TypedConfig<String>("deltaspike.scheduler.DefaultJobFactory",
                "org.quartz.simpl.PropertySettingJobFactory");

        //don't type it to class to keep quartz optional
        TypedConfig<String> JOB_CLASS_NAME =
            new TypedConfig<String>("deltaspike.scheduler.job-class", "org.quartz.Job");
    }

    TypedConfig<String> SCHEDULER_CONFIG_FILE =
        new TypedConfig<String>("deltaspike.scheduler.quartz_config-file", "quartz");

    interface Lifecycle
    {
        TypedConfig<Boolean> START_SCOPES_PER_JOB =
                new TypedConfig<Boolean>("deltaspike.scheduler.start_scopes_for_jobs", Boolean.TRUE);

        TypedConfig<Boolean> FORCE_STOP =
                new TypedConfig<Boolean>("deltaspike.scheduler.force_stop", Boolean.TRUE);

        TypedConfig<Integer> DELAYED_START_IN_SECONDS =
                new TypedConfig<Integer>("deltaspike.scheduler.delayed_start_in_seconds", 1);
    }
}
