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
package org.apache.deltaspike.data.impl;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.AbstractFullEntityRepository;
import org.apache.deltaspike.data.api.Repository;

/**
 * The main extension class for Repositories, based on PartialBeans. Handles following events:<br/>
 * <br/>
 * <b>{@code @Observes BeforeBeanDiscovery}</b>:
 *     Scans the classpath for <code>persistence.xml</code> and extracts relevant information out of it.
 *     This includes mainly entity definitions (type, primary keys) which are not declared with annotations.<br/>
 * <br/>
 * <b>{@code @Observes ProcessAnnotatedType<X>}</b>:
 *     Looks for types annotated with {@link Repository}. Repositories are validated and preprocessed -
 *     all the methods on the repository are checked and analyzed for better runtime performance.<br/>
 * <br/>
 * <b>{@code @Observes AfterBeanDiscovery<X>}</b>:
 *     Raises any definition errors discovered before.
 */
public class RepositoryExtension implements Extension, Deactivatable
{
    private static final Logger LOG = Logger.getLogger(RepositoryExtension.class.getName());

    // TODO: Hack still required?
    private static final ArrayList<Class<?>> REPOSITORY_CLASSES = new ArrayList<Class<?>>();

    private final ArrayList<Class<?>> repositoryClasses = new ArrayList<Class<?>>();
    
    private Boolean isActivated = true;

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery before)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    @SuppressWarnings("unchecked")
    <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event)
    {
        if (!isActivated)
        {
            return;
        }

        if (isVetoed(event.getAnnotatedType()))
        {
            event.veto();
        }
        else if (isRepository(event.getAnnotatedType()))
        {
            Class<X> repositoryClass = event.getAnnotatedType().getJavaClass();

            LOG.log(Level.FINER, "getHandlerClass: Repository annotation detected on {0}",
                    event.getAnnotatedType());
            if (Deactivatable.class.isAssignableFrom(repositoryClass)
                    && !ClassDeactivationUtils.isActivated((Class<? extends Deactivatable>) repositoryClass))
            {
                LOG.log(Level.FINER, "Class {0} is Deactivated", repositoryClass);
                return;
            }

            repositoryClasses.add(repositoryClass);
            REPOSITORY_CLASSES.add(repositoryClass);
        }
    }

    private <X> boolean isRepository(AnnotatedType<X> annotatedType)
    {
        return (annotatedType.isAnnotationPresent(Repository.class) ||
                annotatedType.getJavaClass().isAnnotationPresent(Repository.class)) &&
                !InvocationHandler.class.isAssignableFrom(annotatedType.getJavaClass());
    }

    private <X> boolean isVetoed(AnnotatedType<X> annotated)
    {
        Class<X> javaClass = annotated.getJavaClass();
        return javaClass.equals(AbstractEntityRepository.class) ||
               javaClass.equals(AbstractFullEntityRepository.class);
    }
    
    public ArrayList<Class<?>> getRepositoryClasses()
    {
        ArrayList<Class<?>> result = new ArrayList<Class<?>>();

        if (repositoryClasses.isEmpty() && !REPOSITORY_CLASSES.isEmpty())
        {
            result.addAll(REPOSITORY_CLASSES);
        }

        if (!repositoryClasses.isEmpty())
        {
            result.addAll(repositoryClasses);
        }

        return result;
    }
    
    protected void cleanup(@Observes BeforeShutdown beforeShutdown)
    {
        //we can reset it in any case,
        //because every application produced a copy as application-scoped bean (see RepositoryComponentsFactory)
        REPOSITORY_CLASSES.clear();
    }
}
