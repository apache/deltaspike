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
package org.apache.deltaspike.core.impl.jmx;

import org.apache.deltaspike.core.api.jmx.annotation.MBean;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class MBeanExtension implements Extension, Deactivatable
{
    private static final Logger LOGGER = Logger.getLogger(MBeanExtension.class.getName());

    private final Collection<ObjectName> objectNames = new ArrayList<ObjectName>();

    private Boolean isActivated = true;

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    protected void processBean(@Observes final ProcessManagedBean<?> bean, final BeanManager bm) throws Exception
    {
        if (!isActivated)
        {
            return;
        }

        if (bean.getAnnotated().getAnnotation(MBean.class) != null)
        {
            final ObjectName name = register(bean, bm);
            objectNames.add(name);
            LOGGER.info("Registered MBean " + name.getCanonicalName());
        }
    }

    protected void shutdown(@Observes final BeforeShutdown shutdown) throws Exception
    {
        if (!isActivated)
        {
            return;
        }

        final MBeanServer mBeanServer = mBeanServer();
        for (ObjectName objectName : objectNames)
        {
            mBeanServer.unregisterMBean(objectName);
            LOGGER.info("Unregistered MBean " + objectName.getCanonicalName());
        }
        objectNames.clear();
    }

    private ObjectName register(final ProcessManagedBean<?> bean, final BeanManager bm) throws Exception
    {
        final MBean mBean = bean.getAnnotated().getAnnotation(MBean.class);
        final Class<?> clazz = bean.getAnnotatedBeanClass().getJavaClass();

        String on = mBean.objectName();
        if (on.isEmpty())
        {
            on = "deltaspike:type=MBeans,name=" + clazz.getName();
        }

        final ObjectName objectName = new ObjectName(on);
        mBeanServer().registerMBean(new DynamicMBeanWrapper(clazz,
                isNormalScope(bean.getAnnotatedBeanClass().getJavaClass().getAnnotations(),
                        bean.getAnnotated().getAnnotations(), bm),
                        qualifiers(bean.getAnnotatedBeanClass(), bm)),
                objectName);
        return objectName;
    }

    private Annotation[] qualifiers(final AnnotatedType<?> annotatedBeanClass, final BeanManager bm)
    {
        final Set<Annotation> qualifiers = new HashSet<Annotation>();
        for (Annotation annotation : annotatedBeanClass.getAnnotations())
        {
            if (bm.isQualifier(annotation.annotationType()))
            {
                qualifiers.add(annotation);
            }
        }
        return qualifiers.toArray(new Annotation[qualifiers.size()]);
    }

    // annotated doesn't always contain inherited annotations
    // so test by reflection too
    private boolean isNormalScope(final Annotation[] annotations,
                                  final Set<Annotation> annotationSet,
                                  final BeanManager bm)
    {
        for (Annotation annotation : annotations)
        {
            if (bm.isNormalScope(annotation.annotationType()))
            {
                return true;
            }
        }
        for (Annotation annotation : annotationSet)
        {
            if (bm.isNormalScope(annotation.annotationType()))
            {
                return true;
            }
        }
        return false;
    }

    private MBeanServer mBeanServer()
    {
        return ManagementFactory.getPlatformMBeanServer();
    }
}