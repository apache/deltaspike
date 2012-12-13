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
package org.apache.deltaspike.security.impl.extension;

import org.apache.deltaspike.security.api.authorization.SecurityDefinitionException;
import org.apache.deltaspike.security.impl.util.SecurityUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

class SecurityMetaDataStorage
{
    /**
     * Contains all known authorizers
     */
    private Set<Authorizer> authorizers = new HashSet<Authorizer>();

    /**
     * Contains all known secured methods.
     */
    private Set<AnnotatedMethod<?>> securedMethods = new HashSet<AnnotatedMethod<?>>();

    /**
     * A mapping between a secured method of a class and its authorizers
     */
    private Map<Class<?>, Map<Method, Set<Authorizer>>> methodAuthorizers =
        new HashMap<Class<?>, Map<Method, Set<Authorizer>>>();


    void addAuthorizer(Authorizer authorizer)
    {
        authorizers.add(authorizer);
    }

    void addSecuredType(AnnotatedType<?> annotatedType)
    {
        for (AnnotatedMethod<?> securedMethod : annotatedType.getMethods())
        {
            addSecuredMethod(securedMethod);
        }
    }

    void addSecuredMethod(AnnotatedMethod<?> annotatedMethod)
    {
        securedMethods.add(annotatedMethod);
    }

    Set<AnnotatedMethod<?>> getSecuredMethods()
    {
        return securedMethods;
    }

    void resetSecuredMethods()
    {
        securedMethods = null;
    }

    /**
     * This method is invoked by the security interceptor to obtain the
     * authorizer stack for a secured method
     */
    Set<Authorizer> getAuthorizers(Class<?> targetClass, Method targetMethod)
    {
        if (!isMethodMetaDataAvailable(targetClass, targetMethod))
        {
            registerSecuredMethod(targetClass, targetMethod);
        }

        return getMethodAuthorizers(targetClass, targetMethod);
    }

    void registerSecuredMethods()
    {
        for (AnnotatedMethod<?> method : securedMethods)
        {
            registerSecuredMethod(method.getDeclaringType().getJavaClass(), method.getJavaMember());
        }
    }

    synchronized <T> void registerSecuredMethod(Class<T> targetClass, Method targetMethod)
    {
        ensureInitializedAuthorizersForClass(targetClass);

        if (!containsMethodAuthorizers(targetClass, targetMethod))
        {
            Set<AuthorizationParameter> parameterBindings = new HashSet<AuthorizationParameter>();
            Class<?>[] parameterTypes = targetMethod.getParameterTypes();
            Annotation[][] parameterAnnotations = targetMethod.getParameterAnnotations();
            for (int i = 0; i < parameterTypes.length; i++)
            {
                Set<Annotation> securityBindings = null;
                for (final Annotation parameterAnnotation : parameterAnnotations[i])
                {
                    if (SecurityUtils.isMetaAnnotatedWithSecurityParameterBinding(parameterAnnotation))
                    {
                        if (securityBindings == null)
                        {
                            securityBindings = new HashSet<Annotation>();
                        }
                        securityBindings.add(parameterAnnotation);
                    }
                }
                if (securityBindings != null)
                {
                    parameterBindings.add(new AuthorizationParameter(parameterTypes[i], securityBindings));
                }
            }
            
            Set<Authorizer> authorizerStack = new HashSet<Authorizer>();

            for (Annotation binding : SecurityUtils.getSecurityBindingTypes(targetClass, targetMethod))
            {
                boolean found = false;

                // For each security binding, find a valid authorizer
                for (Authorizer authorizer : authorizers)
                {
                    if (authorizer.matchesBindings(binding, parameterBindings, targetMethod.getReturnType()))
                    {
                        if (found)
                        {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Matching authorizer methods found: [");
                            sb.append(authorizer.getBoundAuthorizerMethod().getDeclaringClass().getName());
                            sb.append(".");
                            sb.append(authorizer.getBoundAuthorizerMethod().getName());
                            sb.append("]");

                            for (Authorizer a : authorizerStack)
                            {
                                if (a.matchesBindings(binding, parameterBindings, targetMethod.getReturnType()))
                                {
                                    sb.append(", [");
                                    sb.append(a.getBoundAuthorizerMethod().getDeclaringClass().getName());
                                    sb.append(".");
                                    sb.append(a.getBoundAuthorizerMethod().getName());
                                    sb.append("]");
                                }
                            }

                            throw new SecurityDefinitionException(
                                    "Ambiguous authorizers found for security binding type [@" +
                                            binding.annotationType().getName() + "] on method [" +
                                            targetMethod.getDeclaringClass().getName() + "." +
                                            targetMethod.getName() + "]. " + sb.toString());
                        }

                        authorizerStack.add(authorizer);
                        found = true;
                    }
                }

                if (!found)
                {
                    throw new SecurityDefinitionException(
                            "No matching authorizer found for security binding type [@" +
                                    binding.annotationType().getName() + "] on method [" +
                                    targetMethod.getDeclaringClass().getName() + "." +
                                    targetMethod.getName() + "].");
                }
            }
            addMethodAuthorizer(targetClass, targetMethod, authorizerStack);
        }
    }

    Set<Authorizer> getAuthorizers()
    {
        return authorizers;
    }

    private boolean containsMethodAuthorizers(Class<?> targetClass, Method targetMethod)
    {
        Map<Method, Set<Authorizer>> resultForClass = methodAuthorizers.get(targetClass);
        return resultForClass.containsKey(targetMethod);
    }

    private void ensureInitializedAuthorizersForClass(Class<?> targetClass)
    {
        Map<Method, Set<Authorizer>> resultForClass = methodAuthorizers.get(targetClass);

        if (resultForClass == null)
        {
            methodAuthorizers.put(targetClass, new HashMap<Method, Set<Authorizer>>());
        }
    }

    private boolean isMethodMetaDataAvailable(Class<?> targetClass, Method targetMethod)
    {
        Map<Method, Set<Authorizer>> result = methodAuthorizers.get(targetClass);
        return result != null && result.containsKey(targetMethod);
    }

    private void addMethodAuthorizer(Class<?> targetClass, Method targetMethod, Set<Authorizer> authorizersToAdd)
    {
        Map<Method, Set<Authorizer>> authorizerMapping = methodAuthorizers.get(targetClass);

        if (authorizerMapping == null)
        {
            authorizerMapping = new HashMap<Method, Set<Authorizer>>();
            methodAuthorizers.put(targetClass, authorizerMapping);
        }

        Set<Authorizer> authorizersForMethod = authorizerMapping.get(targetMethod);

        if (authorizersForMethod == null)
        {
            authorizersForMethod = new HashSet<Authorizer>();
            authorizerMapping.put(targetMethod, authorizersForMethod);
        }

        authorizersForMethod.addAll(authorizersToAdd);
    }

    private Set<Authorizer> getMethodAuthorizers(Class<?> targetClass, Method targetMethod)
    {
        Map<Method, Set<Authorizer>> resultForClass = methodAuthorizers.get(targetClass);

        if (resultForClass == null)
        {
            throw new IllegalStateException(
                    "no meta-data available for: " + targetClass.getName() + targetMethod.getName());
        }

        return resultForClass.get(targetMethod);
    }
}
