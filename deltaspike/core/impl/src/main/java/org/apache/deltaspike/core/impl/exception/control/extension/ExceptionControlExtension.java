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

package org.apache.deltaspike.core.impl.exception.control.extension;

import org.apache.deltaspike.core.api.exception.control.HandlerMethod;
import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.impl.exception.control.HandlerMethodImpl;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ProcessBean;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * CDI extension to find handlers at startup.
 */
@SuppressWarnings({ "unchecked", "CdiManagedBeanInconsistencyInspection" })
public class ExceptionControlExtension implements Extension, Deactivatable
{
    private static final Logger LOG = Logger.getLogger(ExceptionControlExtension.class.getName());

    //this map is application scoped by the def. of the cdi spec.
    //if it needs to be static a classloader key is needed + a cleanup in a BeforeShutdown observer
    private Map<Type, Collection<HandlerMethod<? extends Throwable>>> allHandlers
        = new HashMap<Type, Collection<HandlerMethod<? extends Throwable>>>();

    private Boolean isActivated = true;

    @SuppressWarnings("UnusedDeclaration")
    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    /**
     * Listener to ProcessBean event to locate handlers.
     *
     * @param processBean current {@link AnnotatedType}
     * @param beanManager  Activated Bean Manager
     * @throws TypeNotPresentException if any of the actual type arguments refers to a non-existent type declaration
     *                                 when trying to obtain the actual type arguments from a
     *                                 {@link java.lang.reflect.ParameterizedType}
     * @throws java.lang.reflect.MalformedParameterizedTypeException
     *                                 if any of the actual type parameters refer to a parameterized type that cannot
     *                                 be instantiated for any reason when trying to obtain the actual type arguments
     *                                 from a {@link java.lang.reflect.ParameterizedType}
     */
    @SuppressWarnings("UnusedDeclaration")
    public <T> void findHandlers(@Observes final ProcessBean<?> processBean, final BeanManager beanManager)
    {
        if (!isActivated)
        {
            return;
        }

        if (processBean.getBean() instanceof Interceptor || processBean.getBean() instanceof Decorator ||
                !(processBean.getAnnotated() instanceof AnnotatedType))
        {
            return;
        }

        AnnotatedType annotatedType = (AnnotatedType)processBean.getAnnotated();

        if (annotatedType.getJavaClass().isAnnotationPresent(ExceptionHandler.class))
        {
            final Set<AnnotatedMethod<? super T>> methods = annotatedType.getMethods();

            for (AnnotatedMethod<? super T> method : methods)
            {
                if (HandlerMethodImpl.isHandler(method))
                {
                    if (method.getJavaMember().getExceptionTypes().length != 0)
                    {
                        processBean.addDefinitionError(new IllegalArgumentException(
                            String.format("Handler method %s must not throw exceptions", method.getJavaMember())));
                    }

                    //beanManager won't be stored in the instance -> no issue with wls12c
                    registerHandlerMethod(new HandlerMethodImpl(processBean.getBean(), method, beanManager));
                }
            }
        }
    }

    /**
     * Verifies all injection points for every handler are valid.
     *
     * @param afterDeploymentValidation Lifecycle event
     * @param bm  BeanManager instance
     */
    @SuppressWarnings("UnusedDeclaration")
    public void verifyInjectionPoints(@Observes final AfterDeploymentValidation afterDeploymentValidation,
                                      final BeanManager bm)
    {
        if (!isActivated)
        {
            return;
        }

        for (Map.Entry<Type, Collection<HandlerMethod<? extends Throwable>>> entry : allHandlers.entrySet())
        {
            for (HandlerMethod<? extends Throwable> handler : entry.getValue())
            {
                for (InjectionPoint ip : ((HandlerMethodImpl<? extends Throwable>) handler).getInjectionPoints(bm))
                {
                    try
                    {
                        bm.validate(ip);
                    }
                    catch (InjectionException e)
                    {
                        afterDeploymentValidation.addDeploymentProblem(e);
                    }
                }
            }
        }
    }

    public Map<Type, Collection<HandlerMethod<? extends Throwable>>> getAllExceptionHandlers()
    {
        return Collections.unmodifiableMap(allHandlers);
    }

    private <T extends Throwable> void registerHandlerMethod(HandlerMethod<T> handlerMethod)
    {
        LOG.fine(String.format("Adding handler %s to known handlers", handlerMethod));

        if (allHandlers.containsKey(handlerMethod.getExceptionType()))
        {
            allHandlers.get(handlerMethod.getExceptionType()).add(handlerMethod);
        }
        else
        {
            allHandlers.put(handlerMethod.getExceptionType(),
                new HashSet<HandlerMethod<? extends Throwable>>(Arrays.asList(handlerMethod)));
        }
    }
}
