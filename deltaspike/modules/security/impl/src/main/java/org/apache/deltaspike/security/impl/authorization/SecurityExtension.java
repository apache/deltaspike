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

import org.apache.deltaspike.core.api.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.security.api.authorization.SecurityDefinitionException;
import org.apache.deltaspike.security.api.authorization.annotation.Secures;
import org.apache.deltaspike.security.spi.authentication.Authenticator;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessSessionBean;
import javax.enterprise.inject.spi.SessionBeanType;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension for processing typesafe security annotations
 */
public class SecurityExtension implements Extension, Deactivatable
{
    private static final SecurityInterceptorBinding INTERCEPTOR_BINDING = new SecurityInterceptorBindingLiteral();

    //workaround for OWB
    private static final Map<ClassLoader, SecurityMetaDataStorage> SECURITY_METADATA_STORAGE_MAPPING
        = new ConcurrentHashMap<ClassLoader, SecurityMetaDataStorage>();

    private Boolean isActivated = null;

    //workaround for OWB
    public static SecurityMetaDataStorage getMetaDataStorage()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);

        SecurityMetaDataStorage securityMetaDataStorage = SECURITY_METADATA_STORAGE_MAPPING.get(classLoader);

        if (securityMetaDataStorage == null)
        {
            securityMetaDataStorage = new SecurityMetaDataStorage();
            SECURITY_METADATA_STORAGE_MAPPING.put(classLoader, securityMetaDataStorage);
        }

        return securityMetaDataStorage;
    }

    public static void removeMetaDataStorage()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);
        SECURITY_METADATA_STORAGE_MAPPING.remove(classLoader);
    }

    protected void init(@Observes BeforeBeanDiscovery afterBeanDiscovery)
    {
        initActivation();
    }

    /**
     * @param <X>
     * @param event
     * @param beanManager
     */
    @SuppressWarnings("UnusedDeclaration")
    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event, final BeanManager beanManager)
    {
        if (!this.isActivated)
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
                    registerAuthorizer(m, beanManager);
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
                        isSecured = true;
                        break;
                    }
                }
            }
        }

        // If either the bean or any of its methods are secured, register it
        if (isSecured) 
        {
            getMetaDataStorage().addSecuredType(type);
        }

        if (builder != null) 
        {
            event.setAnnotatedType(builder.create());
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void validateBindings(@Observes AfterBeanDiscovery event, BeanManager beanManager)
    {
        if (!this.isActivated)
        {
            return;
        }

        SecurityMetaDataStorage metaDataStorage = getMetaDataStorage();

        for (final AnnotatedType<?> type : metaDataStorage.getSecuredTypes())
        {
            // Here we simply want to validate that each type that is annotated with
            // one or more security bindings has a valid authorizer for each binding

            for (final Annotation annotation : type.getJavaClass().getAnnotations()) 
            {
                boolean found = false;

                if (SecurityUtils.isMetaAnnotatedWithSecurityBindingType(annotation))
                {
                    // Validate the authorizer
                    for (Authorizer auth : metaDataStorage.getAuthorizers())
                    {
                        if (auth.matchesBinding(annotation)) 
                        {
                            found = true;
                            break;
                        }
                    }

                    if (!found) 
                    {
                        event.addDefinitionError(new SecurityDefinitionException("Secured type " +
                                type.getJavaClass().getName() +
                                " has no matching authorizer method for security binding @" +
                                annotation.annotationType().getName()));
                    }
                }
            }

            for (final AnnotatedMethod<?> method : type.getMethods()) 
            {
                for (final Annotation annotation : method.getAnnotations()) 
                {
                    if (SecurityUtils.isMetaAnnotatedWithSecurityBindingType(annotation))
                    {
                        metaDataStorage.registerSecuredMethod(type.getJavaClass(), method.getJavaMember());
                        break;
                    }
                }
            }
        }

        // Clear securedTypes, we don't require it any more
        metaDataStorage.resetSecuredTypes();
    }

    protected void cleanup(@Observes BeforeShutdown beforeShutdown)
    {
        removeMetaDataStorage();
    }

    /**
     * Registers the specified authorizer method (i.e. a method annotated with
     * the @Secures annotation)
     *
     * @param m
     * @param beanManager
     * @throws SecurityDefinitionException
     */
    private void registerAuthorizer(AnnotatedMethod<?> m, BeanManager beanManager)
    {
        if (!m.getJavaMember().getReturnType().equals(Boolean.class) &&
                !m.getJavaMember().getReturnType().equals(Boolean.TYPE))
        {
            throw new SecurityDefinitionException("Invalid authorizer method [" +
                    m.getJavaMember().getDeclaringClass().getName() + "." +
                    m.getJavaMember().getName() + "] - does not return a boolean.");
        }

        // Locate the binding type
        Annotation binding = null;

        for (Annotation annotation : m.getAnnotations())
        {
            if (SecurityUtils.isMetaAnnotatedWithSecurityBindingType(annotation))
            {
                if (binding != null)
                {
                    throw new SecurityDefinitionException("Invalid authorizer method [" +
                            m.getJavaMember().getDeclaringClass().getName() + "." +
                            m.getJavaMember().getName() + "] - declares multiple security binding types");
                }
                binding = annotation;
            }
        }

        Authorizer authorizer = new Authorizer(binding, m, beanManager);
        getMetaDataStorage().addAuthorizer(authorizer);
    }

    /**
     * Ensures that any implementations of the Authenticator interface are not stateless session beans.
     *
     * @param event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void validateAuthenticatorImplementation(@Observes ProcessSessionBean<Authenticator> event)
    {
        if (!this.isActivated)
        {
            return;
        }

        if (SessionBeanType.STATELESS.equals(event.getSessionBeanType()))
        {
            event.addDefinitionError(new IllegalStateException("Authenticator " + 
                event.getBean().getClass() + " cannot be a Stateless Session Bean"));
        }
    }

    public void initActivation()
    {
        if (isActivated == null)
        {
            isActivated = ClassDeactivationUtils.isActivated(getClass());
        }
    }
}