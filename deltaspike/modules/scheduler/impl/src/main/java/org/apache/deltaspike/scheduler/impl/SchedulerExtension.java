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

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ServiceUtils;
import org.apache.deltaspike.scheduler.api.Scheduled;
import org.apache.deltaspike.scheduler.spi.Scheduler;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SchedulerExtension implements Extension, Deactivatable
{
    private static final Logger LOG = Logger.getLogger(SchedulerExtension.class.getName());

    private Boolean isActivated = true;

    private List<Class> foundManagedJobClasses = new ArrayList<Class>();

    private Scheduler scheduler;

    private Class jobClass;

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());

        if (this.isActivated)
        {
            String jobClassName = SchedulerBaseConfig.Job.JOB_CLASS_NAME.getValue();

            this.jobClass = ClassUtils.tryToLoadClassForName(jobClassName);

            if (this.jobClass == null)
            {
                this.isActivated = false;
            }
        }
    }

    public <X> void findScheduledJobs(@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager)
    {
        if (!this.isActivated)
        {
            return;
        }

        Class<X> beanClass = pat.getAnnotatedType().getJavaClass();

        //see SchedulerProducer
        if (Scheduler.class.isAssignableFrom(beanClass))
        {
            pat.veto();
            return;
        }

        if (!jobClass.isAssignableFrom(beanClass))
        {
            return;
        }

        Scheduled scheduled = pat.getAnnotatedType().getAnnotation(Scheduled.class);
        if (scheduled != null && scheduled.onStartup())
        {
            this.foundManagedJobClasses.add(beanClass);
        }
    }

    public <X> void scheduleJobs(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        if (!this.isActivated)
        {
            return;
        }

        initScheduler(afterBeanDiscovery);

        if (this.scheduler == null)
        {
            return;
        }


        List<String> foundJobNames = new ArrayList<String>();

        for (Class jobClass : this.foundManagedJobClasses)
        {
            if (foundJobNames.contains(jobClass.getSimpleName()))
            {
                afterBeanDiscovery.addDefinitionError(
                    new IllegalStateException("Multiple Job-Classes found with name " + jobClass.getSimpleName()));
            }

            foundJobNames.add(jobClass.getSimpleName());
            this.scheduler.registerNewJob(jobClass);
        }
    }

    public <X> void stopScheduler(@Observes BeforeShutdown beforeShutdown)
    {
        if (!this.isActivated)
        {
            return;
        }

        if (this.scheduler != null)
        {
            this.scheduler.stop();
            this.scheduler = null;
        }
    }

    private void initScheduler(AfterBeanDiscovery afterBeanDiscovery)
    {
        List<Scheduler> availableSchedulers = ServiceUtils.loadServiceImplementations(Scheduler.class, true);

        this.scheduler = findScheduler(availableSchedulers, this.jobClass);

        if (this.scheduler != null)
        {
            try
            {
                this.scheduler.start();
            }
            catch (Throwable t)
            {
                afterBeanDiscovery.addDefinitionError(t);
            }
        }
        else if (this.foundManagedJobClasses.size() > 0)
        {
            LOG.warning(
                this.foundManagedJobClasses.size() + " scheduling-jobs found, but there is no configured scheduler");
        }
    }

    private static Scheduler findScheduler(List<Scheduler> availableSchedulers, Class jobClass)
    {
        for (Scheduler scheduler : availableSchedulers)
        {
            for (Type interfaceClass : scheduler.getClass().getGenericInterfaces())
            {
                if (!(interfaceClass instanceof ParameterizedType) ||
                        !Scheduler.class.isAssignableFrom((Class)((ParameterizedType)interfaceClass).getRawType()))
                {
                    continue;
                }

                if (jobClass.isAssignableFrom(((Class)((ParameterizedType)interfaceClass).getActualTypeArguments()[0])))
                {
                    return scheduler;
                }
            }
        }
        return null;
    }

    Scheduler getScheduler()
    {
        return scheduler;
    }
}
