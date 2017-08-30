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
package org.apache.deltaspike.core.impl.interceptor;

import org.apache.deltaspike.core.api.config.base.CoreBaseConfig;
import org.apache.deltaspike.core.impl.util.AnnotationInstanceUtils;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.interceptor.Interceptor;
import java.lang.annotation.Annotation;
import java.util.logging.Logger;

// promotes deltaspike interceptors to global interceptors in case of cdi 1.1+
public class GlobalInterceptorExtension implements Deactivatable, Extension
{
    private static final Logger LOG = Logger.getLogger(GlobalInterceptorExtension.class.getName());
    private static final String DS_PACKAGE_NAME = "org.apache.deltaspike.";
    private Annotation priorityAnnotationInstance;
    private BeanManager beanManager;

    @SuppressWarnings("UnusedDeclaration")
    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager)
    {
        if (!ClassDeactivationUtils.isActivated(getClass()))
        {
            return;
        }

        this.beanManager = beanManager;
        int priorityValue = CoreBaseConfig.InterceptorCustomization.PRIORITY;
        priorityAnnotationInstance = AnnotationInstanceUtils.getPriorityAnnotationInstance(priorityValue);
    }

    protected void promoteInterceptors(@Observes ProcessAnnotatedType pat)
    {
        if (priorityAnnotationInstance == null) //not CDI 1.1 or the extension is deactivated
        {
            return;
        }

        String beanClassName = pat.getAnnotatedType().getJavaClass().getName();
        if (beanClassName.startsWith(DS_PACKAGE_NAME))
        {
            if (pat.getAnnotatedType().isAnnotationPresent(Interceptor.class))
            {
                //noinspection unchecked
                pat.setAnnotatedType(new GlobalInterceptorWrapper(pat.getAnnotatedType(), priorityAnnotationInstance));
            }
            //currently not needed, because we don't use our interceptors internally -> check for the future
            else if (!beanClassName.contains(".test."))
            {
                for (Annotation annotation : pat.getAnnotatedType().getAnnotations())
                {
                    if (beanManager.isInterceptorBinding(annotation.annotationType()))
                    {
                        //once we see this warning we need to introduce double-call prevention logic due to WELD-1780
                        LOG.warning(beanClassName + " is an bean from DeltaSpike which is intercepted.");
                    }
                }
            }
        }
    }
}
