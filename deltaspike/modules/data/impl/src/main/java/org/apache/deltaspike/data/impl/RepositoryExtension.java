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
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.meta.RepositoryComponentsFactory;
import org.apache.deltaspike.data.impl.meta.unit.PersistenceUnits;

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
 *
 * @author thomashug
 */
public class RepositoryExtension implements Extension
{

    private static final Logger log = Logger.getLogger(RepositoryExtension.class.getName());

    private final List<RepositoryDefinitionException> definitionExceptions =
            new LinkedList<RepositoryDefinitionException>();

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery before)
    {
        PersistenceUnits.instance().init();
    }

    <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event, BeanManager beanManager)
    {
        if (isRepository(event.getAnnotatedType()))
        {
            Class<X> repoClass = event.getAnnotatedType().getJavaClass();
            try
            {
                log.log(Level.FINER, "getHandlerClass: Repository annotation detected on {0}",
                        event.getAnnotatedType());
                RepositoryComponentsFactory.instance().add(repoClass);
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

}
