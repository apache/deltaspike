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

import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.HandlerMethod;
import org.apache.deltaspike.core.impl.exception.control.HandlerMethodImpl;
import org.apache.deltaspike.core.impl.exception.control.HandlerMethodStorage;
import org.apache.deltaspike.core.impl.exception.control.HandlerMethodStorageImpl;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ProcessBean;
import java.lang.reflect.ParameterizedType;
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
public class CatchExtension implements Extension, Deactivatable
{
    private static Map<? super Type, Collection<HandlerMethod<? extends Throwable>>> allHandlers;

    private Logger log = Logger.getLogger(CatchExtension.class.toString());

    public CatchExtension()
    {
        CatchExtension.allHandlers = new HashMap<Type, Collection<HandlerMethod<? extends Throwable>>>();
    }

    /**
     * Listener to ProcessBean event to locate handlers.
     *
     * @param pmb Event from CDI SPI
     * @param bm  Activated Bean Manager
     * @throws TypeNotPresentException if any of the actual type arguments refers to a non-existent type declaration
     *                                 when trying to obtain the actual type arguments from a {@link ParameterizedType}
     * @throws java.lang.reflect.MalformedParameterizedTypeException
     *                                 if any of the actual type parameters refer to a parameterized type that cannot
     *                                 be instantiated for any reason when trying to obtain the actual type arguments
     *                                 from a {@link ParameterizedType}
     */
    public <T> void findHandlers(@Observes final ProcessBean<?> pmb, final BeanManager bm)
    {
        if (!ClassDeactivationUtils.isActivated(CatchExtension.class))
        {
            return;
        }

        if (!(pmb.getAnnotated() instanceof AnnotatedType) || pmb.getBean() instanceof Interceptor ||
                pmb.getBean() instanceof Decorator)
        {
            return;
        }

        final AnnotatedType<T> type = (AnnotatedType<T>) pmb.getAnnotated();

        if (type.getJavaClass().isAnnotationPresent(ExceptionHandler.class))
        {
            final Set<AnnotatedMethod<? super T>> methods = type.getMethods();

            for (AnnotatedMethod<? super T> method : methods)
            {
                if (HandlerMethodImpl.isHandler(method))
                {
                    final AnnotatedParameter<?> param = HandlerMethodImpl.findHandlerParameter(method);
                    if (method.getJavaMember().getExceptionTypes().length != 0)
                    {
                        pmb.addDefinitionError(new IllegalArgumentException(
                                String.format("Handler method %s must not throw exceptions", method.getJavaMember())));
                    }
                    final Class<? extends Throwable> exceptionType = (Class<? extends Throwable>) ((ParameterizedType)
                            param.getBaseType()).getActualTypeArguments()[0];

                    registerHandlerMethod(new HandlerMethodImpl(method, bm));
                }
            }
        }
    }

    /**
     * Verifies all injection points for every handler are valid.
     *
     * @param adv Lifecycle event
     * @param bm  BeanManager instance
     */
    public void verifyInjectionPoints(@Observes final AfterDeploymentValidation adv, final BeanManager bm)
    {
        if (!ClassDeactivationUtils.isActivated(CatchExtension.class))
        {
            return;
        }

        for (Map.Entry<? super Type, Collection<HandlerMethod<? extends Throwable>>> entry : allHandlers.entrySet())
        {
            for (HandlerMethod<? extends Throwable> handler : entry.getValue())
            {
                for (InjectionPoint ip : ((HandlerMethodImpl<? extends Throwable>) handler).getInjectionPoints())
                {
                    try
                    {
                        bm.validate(ip);
                    }
                    catch (InjectionException e)
                    {
                        adv.addDeploymentProblem(e);
                    }
                }
            }
        }
    }

    public static HandlerMethodStorage createStorage()
    {
        return new HandlerMethodStorageImpl(Collections.unmodifiableMap(CatchExtension.allHandlers));
    }

    private <T extends Throwable> void registerHandlerMethod(HandlerMethod<T> handlerMethod)
    {
        log.fine(String.format("Adding handler %s to known handlers", handlerMethod));
        if (CatchExtension.allHandlers.containsKey(handlerMethod.getExceptionType()))
        {
            CatchExtension.allHandlers.get(handlerMethod.getExceptionType()).add(handlerMethod);
        }
        else
        {
            CatchExtension.allHandlers.put(handlerMethod.getExceptionType(),
                    new HashSet<HandlerMethod<? extends Throwable>>(Arrays.asList(handlerMethod)));
        }
    }
}
