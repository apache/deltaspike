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

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ParentExtensionStorage;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.security.api.authorization.Secures;
import org.apache.deltaspike.security.api.authorization.SecurityDefinitionException;
import org.apache.deltaspike.security.impl.util.SecurityUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * Extension for processing typesafe security annotations
 */
public class SecurityExtension implements Extension, Deactivatable
{
    private static final SecurityInterceptorBinding INTERCEPTOR_BINDING = new SecurityInterceptorBindingLiteral();

    private SecurityMetaDataStorage securityMetaDataStorage;

    private Boolean isActivated = null;

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
        securityMetaDataStorage = new SecurityMetaDataStorage();
        ParentExtensionStorage.addExtension(this);
    }

    //workaround for OWB
    public SecurityMetaDataStorage getMetaDataStorage()
    {
        return securityMetaDataStorage;
    }

    /**
     * Handles &#064;Secured beans
     */
    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event)
    {
        if (!isActivated)
        {
            return;
        }

        AnnotatedTypeBuilder<X> builder = null;
        AnnotatedType<X> type = event.getAnnotatedType();
        
        boolean isSecured = false;

        // Add the security interceptor to the class if the class is annotated
        // with a security binding type
        for (final Annotation annotation : type.getAnnotations())
        {
            if (SecurityUtils.isMetaAnnotatedWithSecurityBindingType(annotation))
            {
                builder = new AnnotatedTypeBuilder<X>().readFromType(type);
                builder.addToClass(INTERCEPTOR_BINDING);
                getMetaDataStorage().addSecuredType(type);
                isSecured = true;
                break;
            }
        }

        // If the class isn't annotated with a security binding type, check if
        // any of its methods are, and if so, add the security interceptor to the
        // method
        if (!isSecured) 
        {
            for (final AnnotatedMethod<? super X> m : type.getMethods()) 
            {
                if (m.isAnnotationPresent(Secures.class))
                {
                    registerAuthorizer(m);
                    continue;
                }

                for (final Annotation annotation : m.getAnnotations()) 
                {
                    if (SecurityUtils.isMetaAnnotatedWithSecurityBindingType(annotation))
                    {
                        if (builder == null) 
                        {
                            builder = new AnnotatedTypeBuilder<X>().readFromType(type);
                        }
                        builder.addToMethod(m, INTERCEPTOR_BINDING);
                        getMetaDataStorage().addSecuredMethod(m);
                        break;
                    }
                }
            }
        }

        if (builder != null) 
        {
            event.setAnnotatedType(builder.create());
        }
    }

    public void validateBindings(@Observes AfterBeanDiscovery event, BeanManager beanManager)
    {
        if (!isActivated)
        {
            return;
        }

        SecurityMetaDataStorage metaDataStorage = getMetaDataStorage();

        SecurityExtension parentExtension = ParentExtensionStorage.getParentExtension(this);
        if (parentExtension != null)
        {
            // also add the authorizers from the parent extension
            Set<Authorizer> parentAuthorizers = parentExtension.getMetaDataStorage().getAuthorizers();
            for (Authorizer parentAuthorizer : parentAuthorizers)
            {
                metaDataStorage.addAuthorizer(parentAuthorizer);
            }
        }

        metaDataStorage.registerSecuredMethods();

        for (final AnnotatedMethod<?> method : metaDataStorage.getSecuredMethods())
        {
            // Here we simply want to validate that each method that is annotated with
            // one or more security bindings has a valid authorizer for each binding

            Class<?> targetClass = method.getDeclaringType().getJavaClass();
            Method targetMethod = method.getJavaMember();
            for (final Annotation annotation : SecurityUtils.getSecurityBindingTypes(targetClass, targetMethod)) 
            {
                boolean found = false;

                Set<AuthorizationParameter> authorizationParameters = new HashSet<AuthorizationParameter>();
                for (AnnotatedParameter<?> parameter : (List<AnnotatedParameter<?>>) (List<?>) method.getParameters())
                {
                    Set<Annotation> securityParameterBindings = null;
                    for (Annotation a : parameter.getAnnotations())
                    {
                        if (SecurityUtils.isMetaAnnotatedWithSecurityParameterBinding(a))
                        {
                            if (securityParameterBindings == null)
                            {
                                securityParameterBindings = new HashSet<Annotation>();
                            }
                            securityParameterBindings.add(a);
                        }
                    }
                    if (securityParameterBindings != null)
                    {
                        AuthorizationParameter authorizationParameter
                            = new AuthorizationParameter(parameter.getBaseType(), securityParameterBindings);
                        authorizationParameters.add(authorizationParameter);
                    }
                }
                // Validate the authorizer
                for (Authorizer auth : metaDataStorage.getAuthorizers())
                {
                    if (auth.matchesBindings(annotation, authorizationParameters, targetMethod.getReturnType())) 
                    {
                        found = true;
                        break;
                    }
                }

                if (!found) 
                {
                    event.addDefinitionError(new SecurityDefinitionException("Secured type " +
                            method.getDeclaringType().getJavaClass().getName() +
                            " has no matching authorizer method for security binding @" +
                            annotation.annotationType().getName()));
                }
            }

            for (final Annotation annotation : method.getAnnotations()) 
            {
                if (SecurityUtils.isMetaAnnotatedWithSecurityBindingType(annotation))
                {
                    metaDataStorage.registerSecuredMethod(targetClass, targetMethod);
                    break;
                }
            }
        }

        // Clear securedTypes, we don't require it any more
        metaDataStorage.resetSecuredMethods();
    }

    /**
     * Registers the specified authorizer method (i.e. a method annotated with
     * the @Secures annotation)
     *
     * @throws SecurityDefinitionException
     */
    private void registerAuthorizer(AnnotatedMethod<?> annotatedMethod)
    {
        if (!annotatedMethod.getJavaMember().getReturnType().equals(Boolean.class) &&
                !annotatedMethod.getJavaMember().getReturnType().equals(Boolean.TYPE))
        {
            throw new SecurityDefinitionException("Invalid authorizer method [" +
                    annotatedMethod.getJavaMember().getDeclaringClass().getName() + "." +
                    annotatedMethod.getJavaMember().getName() + "] - does not return a boolean.");
        }

        // Locate the binding type
        Annotation binding = null;

        for (Annotation annotation : annotatedMethod.getAnnotations())
        {
            if (SecurityUtils.isMetaAnnotatedWithSecurityBindingType(annotation))
            {
                if (binding != null)
                {
                    throw new SecurityDefinitionException("Invalid authorizer method [" +
                            annotatedMethod.getJavaMember().getDeclaringClass().getName() + "." +
                            annotatedMethod.getJavaMember().getName() + "] - declares multiple security binding types");
                }
                binding = annotation;
            }
        }

        Authorizer authorizer = new Authorizer(binding, annotatedMethod);
        getMetaDataStorage().addAuthorizer(authorizer);
    }
}
