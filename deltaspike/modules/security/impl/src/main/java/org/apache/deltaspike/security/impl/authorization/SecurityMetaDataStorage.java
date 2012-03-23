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

import org.apache.deltaspike.security.api.authorization.SecurityDefinitionException;

import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
class SecurityMetaDataStorage
{
    /**
     * Contains all known authorizers
     */
    private Set<Authorizer> authorizers = new HashSet<Authorizer>();

    /**
     * Contains all known secured types
     */
    private Set<AnnotatedType<?>> securedTypes = new HashSet<AnnotatedType<?>>();

    /**
     * A mapping between a secured method of a class and its authorizers
     */
    private Map<Class<?>, Map<Method, Set<Authorizer>>> methodAuthorizers =
        new HashMap<Class<?>, Map<Method, Set<Authorizer>>>();


    void addAuthorizer(Authorizer authorizer)
    {
        this.authorizers.add(authorizer);
    }

    void addSecuredType(AnnotatedType<?> annotatedType)
    {
        this.securedTypes.add(annotatedType);
    }

    Set<AnnotatedType<?>> getSecuredTypes()
    {
        return securedTypes;
    }

    void resetSecuredTypes()
    {
        this.securedTypes = null;
    }

    /**
     * This method is invoked by the security interceptor to obtain the
     * authorizer stack for a secured method
     *
     * @param targetClass
     * @param targetMethod
     * @return
     */
    Set<Authorizer> getAuthorizers(Class<?> targetClass, Method targetMethod)
    {
        if (!isMethodMetaDataAvailable(targetClass, targetMethod))
        {
            registerSecuredMethod(targetClass, targetMethod);
        }

        return getMethodAuthorizers(targetClass, targetMethod);
    }

    synchronized void registerSecuredMethod(Class<?> targetClass, Method targetMethod)
    {
        ensureInitializedAuthorizersForClass(targetClass);

        if (!containsMethodAuthorizers(targetClass, targetMethod))
        {
            // Build a list of all security bindings on both the method and its declaring class
            Set<Annotation> bindings = new HashSet<Annotation>();

            Class<?> cls = targetClass;
            while (!cls.equals(Object.class))
            {
                for (final Annotation annotation : cls.getAnnotations())
                {
                    if (SecurityUtils.isMetaAnnotatedWithSecurityBindingType(annotation))
                    {
                        bindings.add(annotation);
                    }
                }
                cls = cls.getSuperclass();
            }

            for (final Annotation annotation : targetMethod.getAnnotations())
            {
                if (SecurityUtils.isMetaAnnotatedWithSecurityBindingType(annotation))
                {
                    bindings.add(annotation);
                }
            }

            Set<Authorizer> authorizerStack = new HashSet<Authorizer>();

            for (Annotation binding : bindings)
            {
                boolean found = false;

                // For each security binding, find a valid authorizer
                for (Authorizer authorizer : this.authorizers)
                {
                    if (authorizer.matchesBinding(binding))
                    {
                        if (found)
                        {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Matching authorizer methods found: [");
                            sb.append(authorizer.getImplementationMethod().getDeclaringClass().getName());
                            sb.append(".");
                            sb.append(authorizer.getImplementationMethod().getName());
                            sb.append("]");

                            for (Authorizer a : authorizerStack)
                            {
                                if (a.matchesBinding(binding))
                                {
                                    sb.append(", [");
                                    sb.append(a.getImplementationMethod().getDeclaringClass().getName());
                                    sb.append(".");
                                    sb.append(a.getImplementationMethod().getName());
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
        Map<Method, Set<Authorizer>> resultForClass = this.methodAuthorizers.get(targetClass);
        return resultForClass.containsKey(targetMethod);
    }

    private void ensureInitializedAuthorizersForClass(Class<?> targetClass)
    {
        Map<Method, Set<Authorizer>> resultForClass = this.methodAuthorizers.get(targetClass);

        if (resultForClass == null)
        {
            this.methodAuthorizers.put(targetClass, new HashMap<Method, Set<Authorizer>>());
        }
    }

    private boolean isMethodMetaDataAvailable(Class<?> targetClass, Method targetMethod)
    {
        Map<Method, Set<Authorizer>> result = this.methodAuthorizers.get(targetClass);
        return result != null && result.containsKey(targetMethod);
    }

    private void addMethodAuthorizer(Class<?> targetClass, Method targetMethod, Set<Authorizer> authorizersToAdd)
    {
        Map<Method, Set<Authorizer>> authorizerMapping = this.methodAuthorizers.get(targetClass);

        if (authorizerMapping == null)
        {
            authorizerMapping = new HashMap<Method, Set<Authorizer>>();
            this.methodAuthorizers.put(targetClass, authorizerMapping);
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
        Map<Method, Set<Authorizer>> resultForClass = this.methodAuthorizers.get(targetClass);

        if (resultForClass == null)
        {
            throw new IllegalStateException(
                    "no meta-data available for: " + targetClass.getName() + targetMethod.getName());
        }

        return resultForClass.get(targetMethod);
    }
}
