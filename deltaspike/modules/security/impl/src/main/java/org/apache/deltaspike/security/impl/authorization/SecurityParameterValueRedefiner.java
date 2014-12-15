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
package org.apache.deltaspike.security.impl.authorization;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.interceptor.InvocationContext;

import org.apache.deltaspike.core.util.AnnotationUtils;
import org.apache.deltaspike.core.util.metadata.builder.ParameterValueRedefiner;
import org.apache.deltaspike.security.api.authorization.SecuredReturn;
import org.apache.deltaspike.security.api.authorization.SecurityParameterBinding;

/**
 * Responsible for supplying requested method invocation values to the security binding method.
 */
public class SecurityParameterValueRedefiner implements ParameterValueRedefiner
{
    private CreationalContext<?> creationalContext;
    private InvocationContext invocation;
    private Object result;

    public SecurityParameterValueRedefiner(CreationalContext<?> creationalContext,
                                           InvocationContext invocation,
                                           Object result)
    {
        this.creationalContext = creationalContext;
        this.invocation = invocation;
        this.result = result;
    }

    @Override
    public Object redefineParameterValue(ParameterValue value)
    {

        InjectionPoint injectionPoint = value.getInjectionPoint();
        if (injectionPoint != null)
        {
            if (value.getInjectionPoint().getAnnotated().getBaseType().equals(InvocationContext.class))
            {
                return invocation;
            }
            else if (value.getInjectionPoint().getAnnotated().isAnnotationPresent(SecuredReturn.class))
            {
                return result;
            }
            else
            {
                Annotated securingParameterAnnotatedType = injectionPoint.getAnnotated();
                Set<Annotation> securingParameterAnnotations = securingParameterAnnotatedType.getAnnotations();

                Set<Annotation> requiredBindingAnnotations = new HashSet<Annotation>();
                for (Annotation annotation : securingParameterAnnotations)
                {
                    if (annotation.annotationType().isAnnotationPresent(SecurityParameterBinding.class))
                    {
                        requiredBindingAnnotations.add(annotation);
                    }
                }

                if (!requiredBindingAnnotations.isEmpty())
                {
                    Method method = invocation.getMethod();
                    Annotation[][] businessMethodParameterAnnotations = method.getParameterAnnotations();
                    for (int i = 0; i < businessMethodParameterAnnotations.length; i++)
                    {
                        List<Annotation> businessParameterAnnotations = Arrays
                                    .asList(businessMethodParameterAnnotations[i]);
                        for (Annotation annotation : requiredBindingAnnotations)
                        {
                            if (businessParameterAnnotations.contains(annotation))
                            {
                                return invocation.getParameters()[i];
                            }
                        }

                        Set<Integer> hashCodesOfBusinessParameterAnnotations =
                            new HashSet<Integer>(businessParameterAnnotations.size());

                        for (Annotation annotation : businessParameterAnnotations)
                        {
                            hashCodesOfBusinessParameterAnnotations.add(
                                AnnotationUtils.getQualifierHashCode(annotation));
                        }
                        //2nd try (detailed check)
                        for (Annotation annotation : requiredBindingAnnotations)
                        {
                            if (hashCodesOfBusinessParameterAnnotations.contains(
                                AnnotationUtils.getQualifierHashCode(annotation)))
                            {
                                return invocation.getParameters()[i];
                            }
                        }
                    }

                    throw new IllegalStateException("Missing required security parameter binding "
                                + requiredBindingAnnotations + " on method invocation ["
                                + method.getDeclaringClass().getName() + "." + method.getName()
                                + Arrays.asList(method.getParameterTypes()).toString().replaceFirst("\\[", "(")
                                            .replaceFirst("\\]$", ")") + "]");

                }
            }
        }

        return value.getDefaultValue(creationalContext);
    }
}
