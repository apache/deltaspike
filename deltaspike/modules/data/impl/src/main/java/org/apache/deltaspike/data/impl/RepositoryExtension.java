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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.AbstractFullEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.meta.RepositoryComponents;

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

    private static final Logger log = Logger.getLogger(RepositoryExtension.class.getName());

    private static RepositoryComponents staticComponents = new RepositoryComponents();

    private final List<RepositoryDefinitionException> definitionExceptions =
            new LinkedList<RepositoryDefinitionException>();

    private Boolean isActivated = true;

    private RepositoryComponents components = new RepositoryComponents();

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
            Class<X> repoClass = event.getAnnotatedType().getJavaClass();
            try
            {
                log.log(Level.FINER, "getHandlerClass: Repository annotation detected on {0}",
                        event.getAnnotatedType());
                if (Deactivatable.class.isAssignableFrom(repoClass)
                        && !ClassDeactivationUtils.isActivated((Class<? extends Deactivatable>) repoClass))
                {
                    log.log(Level.FINER, "Class {0} is Deactivated", repoClass);
                    return;
                }
                components.add(repoClass);
                staticComponents.add(repoClass);
            }
            catch (RepositoryDefinitionException e)
            {
                definitionExceptions.add(e);
            }
            catch (Exception e)
            {
                definitionExceptions.add(new RepositoryDefinitionException(repoClass, e));
            }
        }
    }

    <X> void addDefinitionErrors(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        if (!isActivated)
        {
            return;
        }

        for (RepositoryDefinitionException ex : definitionExceptions)
        {
            afterBeanDiscovery.addDefinitionError(ex);
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

    public RepositoryComponents getComponents()
    {
        RepositoryComponents result = new RepositoryComponents();
        if (components.getRepositories().isEmpty() && !staticComponents.getRepositories().isEmpty())
        {
            result.addAll(staticComponents.getRepositories());
        }

        if (!components.getRepositories().isEmpty())
        {
            result.addAll(components.getRepositories());
        }

        return result;
    }

    protected void cleanup(@Observes BeforeShutdown beforeShutdown)
    {
        //we can reset it in any case,
        //because every application produced a copy as application-scoped bean (see RepositoryComponentsFactory)
        staticComponents.getRepositories().clear();
    }
}
