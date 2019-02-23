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

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.core.util.ServiceUtils;
import org.apache.deltaspike.scheduler.api.Scheduled;
import org.apache.deltaspike.scheduler.spi.SchedulerControl;
import org.apache.deltaspike.scheduler.spi.Scheduler;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class SchedulerExtension implements Extension, Deactivatable
{
    private static final Logger LOG = Logger.getLogger(SchedulerExtension.class.getName());

    //keep it as a string (needed by some containers - due to the imports)
    private static Set<String> classNamesToVeto = new HashSet<String>();

    private Boolean isActivated = true;

    private Set<Class> foundManagedJobClasses = new HashSet<Class>();

    private Scheduler scheduler;

    private Class jobClass;

    public SchedulerExtension()
    {
        classNamesToVeto.add("org.apache.deltaspike.scheduler.impl.DynamicExpressionObserverJob");
    }

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());

        if (this.isActivated)
        {
            String jobClassName = SchedulerBaseConfig.JobCustomization.JOB_CLASS_NAME;

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
        if (Scheduler.class.isAssignableFrom(beanClass) || isInternalUnmanagedClass(beanClass))
        {
            pat.veto();
            return;
        }

        if (!jobClass.isAssignableFrom(beanClass) && !Runnable.class.isAssignableFrom(beanClass))
        {
            return;
        }

        Scheduled scheduled = pat.getAnnotatedType().getAnnotation(Scheduled.class);
        if (scheduled != null && scheduled.onStartup())
        {
            this.foundManagedJobClasses.add(beanClass);
        }
    }

    private <X> boolean isInternalUnmanagedClass(Class<X> beanClass)
    {
        return classNamesToVeto.contains(beanClass.getName());
    }

    public <X> void validateJobs(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        if (!this.isActivated)
        {
            return;
        }

        Class configuredJobClass = this.jobClass;

        this.jobClass = resolveFinalJobType();

        if (this.jobClass == null)
        {
            afterBeanDiscovery.addDefinitionError(new IllegalStateException("Please only annotate classes with @" +
                Scheduled.class.getName() + " of type " +
                configuredJobClass.getName() + " or of type " + Runnable.class.getName() + ", but not both!"));
            return;
        }

    }

    public <X> void scheduleJobs(@Observes AfterDeploymentValidation afterDeploymentValidation, BeanManager beanManager)
    {
        if (!this.isActivated)
        {
            return;
        }

        SchedulerControl schedulerControl = BeanProvider.getContextualReference(SchedulerControl.class, true);
        if (schedulerControl != null && !schedulerControl.isSchedulerEnabled())
        {
            LOG.info("Scheduler has been disabled by " + ProxyUtils.getUnproxiedClass(schedulerControl.getClass()));
            return;
        }

        initScheduler(afterDeploymentValidation);

        if (this.scheduler == null)
        {
            return;
        }


        List<String> foundJobNames = new ArrayList<String>();

        for (Class jobClass : this.foundManagedJobClasses)
        {
            if (foundJobNames.contains(jobClass.getSimpleName()))
            {
                afterDeploymentValidation.addDeploymentProblem(
                    new IllegalStateException("Multiple Job-Classes found with name " + jobClass.getSimpleName()));
            }

            foundJobNames.add(jobClass.getSimpleName());
            this.scheduler.registerNewJob(jobClass);
        }
    }

    /**
     * Allows to support implementations of {@link java.lang.Runnable}
     * annotated with {@link Scheduled} >without< explicit config.
     * @return the job-type which will be used to select the scheduler
     */
    protected Class resolveFinalJobType()
    {
        Set<Class> foundTypes = new HashSet<Class>();

        for (Class foundJobClass : this.foundManagedJobClasses)
        {
            if (jobClass.isAssignableFrom(foundJobClass))
            {
                foundTypes.add(jobClass);
            }
            else if (Runnable.class.isAssignableFrom(foundJobClass))
            {
                foundTypes.add(Runnable.class);
            }
        }

        if (foundTypes.size() > 1)
        {
            return null;
        }
        else if (foundTypes.size() == 1)
        {
            return foundTypes.iterator().next();
        }
        else
        {
            //use the configured type
            //it's still useful if there is no annotated job-class, but a dyn. usage of the scheduler is still possible
            return jobClass;
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

    private void initScheduler(AfterDeploymentValidation afterDeploymentValidation)
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
                afterDeploymentValidation.addDeploymentProblem(t);
            }
        }
        else if (!this.foundManagedJobClasses.isEmpty())
        {
            LOG.warning(
                this.foundManagedJobClasses.size() + " scheduling-jobs found, but there is no configured scheduler");
        }
    }

    private static Scheduler findScheduler(List<Scheduler> availableSchedulers, Class jobClass)
    {
        for (Scheduler scheduler : availableSchedulers)
        {
            //in case of implementing the Scheduler interface directly
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

            //in case of extending e.g. AbstractQuartzScheduler
            if (scheduler.getClass().getGenericSuperclass() instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) scheduler.getClass().getGenericSuperclass();
                for (Type typeArgument : parameterizedType.getActualTypeArguments())
                {
                    if (jobClass.isAssignableFrom((Class)typeArgument))
                    {
                        return scheduler;
                    }
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
