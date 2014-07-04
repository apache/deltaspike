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
package org.apache.deltaspike.example.scheduler;

import org.apache.deltaspike.scheduler.api.Scheduled;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;
import java.util.logging.Logger;

@Scheduled(cronExpression = "0/1 * * * * ?")
public class SimpleSchedulerJob2 implements Job
{
    private static final Logger LOG = Logger.getLogger(SimpleSchedulerJob2.class.getName());

    @Inject
    private GlobalResultHolder globalResultHolder;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        LOG.info("#increase called by " + getClass().getName());
        globalResultHolder.increase();
    }
}
