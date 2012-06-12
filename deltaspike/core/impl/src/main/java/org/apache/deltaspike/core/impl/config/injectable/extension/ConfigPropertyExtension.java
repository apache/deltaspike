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
package org.apache.deltaspike.core.impl.config.injectable.extension;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.impl.config.injectable.ConfigPropertyBean;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Adds support for {@link org.apache.deltaspike.core.api.config.annotation.ConfigProperty}
 */
public class ConfigPropertyExtension implements Extension, Deactivatable
{
    private Boolean isActivated = null;

    private Set<InjectionTargetEntry> injectionTargets = new HashSet<InjectionTargetEntry>();

    @SuppressWarnings("UnusedDeclaration")
    protected void recordConfigPropertyAwareInjectionPoint(@Observes ProcessInjectionTarget<?> event)
    {
        initActivation();

        if (!isActivated)
        {
            return;
        }

        InjectionTarget<?> injectionTarget = event.getInjectionTarget();

        ConfigProperty configProperty;
        Annotation qualifier;
        for (InjectionPoint injectionPoint : injectionTarget.getInjectionPoints())
        {
            qualifier = null;
            configProperty = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);

            if (configProperty == null)
            {
                for (Annotation annotation : injectionPoint.getAnnotated().getAnnotations())
                {
                    configProperty = annotation.annotationType().getAnnotation(ConfigProperty.class);

                    if (configProperty != null)
                    {
                        qualifier = annotation;
                        break;
                    }
                }
            }

            if (configProperty != null)
            {
                //TODO add support for collections,...
                if (configProperty.eager() && ConfigResolver.getPropertyValue(configProperty.name()) == null)
                {
                    throw new IllegalStateException("no configured value found for property: " + configProperty.name());
                }

                injectionTargets.add(
                        new InjectionTargetEntry(injectionPoint.getType(), configProperty, qualifier));
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void addDependentBeans(@Observes AfterBeanDiscovery event)
    {
        initActivation();

        if (!isActivated)
        {
            return;
        }

        for (InjectionTargetEntry injectionTargetEntry : injectionTargets)
        {
            event.addBean(new ConfigPropertyBean<Object>(injectionTargetEntry.getType(), injectionTargetEntry
                    .getConfigProperty(), injectionTargetEntry.getCustomQualifier()));
        }
    }

    protected void initActivation()
    {
        if (isActivated == null)
        {
            isActivated = ClassDeactivationUtils.isActivated(getClass());
        }
    }
}
