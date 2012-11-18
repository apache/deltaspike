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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InvocationContext;

import org.apache.deltaspike.core.util.metadata.builder.InjectableMethod;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.apache.deltaspike.security.api.authorization.SecurityDefinitionException;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.apache.deltaspike.security.api.authorization.annotation.SecurityBindingType;
import org.apache.deltaspike.security.impl.authorization.SecurityParameterValueRedefiner;
import org.apache.deltaspike.security.impl.util.SecurityUtils;

/**
 * Responsible for authorizing method invocations.
 */
@Typed()
class Authorizer
{
    private Annotation bindingAnnotation;
    private Map<Method, Object> bindingSecurityBindingMembers = new HashMap<Method, Object>();
    private Set<AuthorizationParameter> authorizationParameters = new HashSet<AuthorizationParameter>();

    private AnnotatedMethod<?> boundAuthorizerMethod;
    private volatile Bean<?> boundAuthorizerBean;

    private volatile InjectableMethod<?> boundAuthorizerMethodProxy;

    Authorizer(Annotation bindingAnnotation, AnnotatedMethod<?> boundAuthorizerMethod)
    {
        this.bindingAnnotation = bindingAnnotation;
        this.boundAuthorizerMethod = boundAuthorizerMethod;

        try
        {
            for (Method method : bindingAnnotation.annotationType().getDeclaredMethods())
            {
                if (method.isAnnotationPresent(Nonbinding.class))
                {
                    continue;
                }
                bindingSecurityBindingMembers.put(method, method.invoke(bindingAnnotation));
            }
        }
        catch (InvocationTargetException ex)
        {
            throw new SecurityDefinitionException("Error reading security binding members", ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new SecurityDefinitionException("Error reading security binding members", ex);
        }
        
        for (AnnotatedParameter<?> annotatedParameter : boundAuthorizerMethod.getParameters())
        {
            Set<Annotation> securityParameterBindings = null;
            for (Annotation annotation : annotatedParameter.getAnnotations())
            {
                if (SecurityUtils.isMetaAnnotatedWithSecurityParameterBinding(annotation))
                {
                    if (securityParameterBindings == null)
                    {
                        securityParameterBindings = new HashSet<Annotation>();
                    }
                    securityParameterBindings.add(annotation);
                }
            }
            if (securityParameterBindings != null)
            {
                AuthorizationParameter authorizationParameter
                    = new AuthorizationParameter(annotatedParameter.getBaseType(), securityParameterBindings);
                authorizationParameters.add(authorizationParameter);
            }
        }
    }

    void authorize(final InvocationContext ic, BeanManager beanManager)
    {
        if (boundAuthorizerBean == null)
        {
            lazyInitTargetBean(beanManager);
        }

        final CreationalContext<?> creationalContext = beanManager.createCreationalContext(boundAuthorizerBean);

        Object reference = beanManager.getReference(boundAuthorizerBean,
            boundAuthorizerMethod.getJavaMember().getDeclaringClass(), creationalContext);

        Object result = boundAuthorizerMethodProxy.invoke(reference, creationalContext, 
                    new SecurityParameterValueRedefiner(creationalContext, ic));

        if (result.equals(Boolean.FALSE))
        {
            Set<SecurityViolation> violations = new HashSet<SecurityViolation>();
            violations.add(new SecurityViolation()
            {
                private static final long serialVersionUID = 2358753444038521129L;

                @Override
                public String getReason()
                {
                    return "Authorization check failed";
                }
            });

            throw new AccessDeniedException(violations);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private synchronized void lazyInitTargetBean(BeanManager beanManager)
    {
        if (boundAuthorizerBean == null)
        {
            Method method = boundAuthorizerMethod.getJavaMember();

            Set<Bean<?>> beans = beanManager.getBeans(method.getDeclaringClass());
            boundAuthorizerBean = beanManager.resolve(beans);

            if (boundAuthorizerBean == null)
            {
                throw new IllegalStateException("Exception looking up authorizer method bean - " +
                        "no beans found for method [" + method.getDeclaringClass() + "." +
                        method.getName() + "]");
            }

            boundAuthorizerMethodProxy = new InjectableMethod(boundAuthorizerMethod, boundAuthorizerBean, beanManager);
        }
    }

    boolean matchesBindings(Annotation annotation, Set<AuthorizationParameter> parameterBindings)
    {
        if (!annotation.annotationType().isAnnotationPresent(SecurityBindingType.class) &&
                annotation.annotationType().isAnnotationPresent(Stereotype.class))
        {
            annotation = SecurityUtils.resolveSecurityBindingType(annotation);
        }

        if (!annotation.annotationType().equals(bindingAnnotation.annotationType()))
        {
            return false;
        }

        for (Method method : annotation.annotationType().getDeclaredMethods())
        {
            if (method.isAnnotationPresent(Nonbinding.class))
            {
                continue;
            }

            if (!bindingSecurityBindingMembers.containsKey(method))
            {
                return false;
            }

            try
            {
                Object value = method.invoke(annotation);
                if (!bindingSecurityBindingMembers.get(method).equals(value))
                {
                    return false;
                }
            }
            catch (InvocationTargetException ex)
            {
                throw new SecurityDefinitionException("Error reading security binding members", ex);
            }
            catch (IllegalAccessException ex)
            {
                throw new SecurityDefinitionException("Error reading security binding members", ex);
            }
        }

        for (AuthorizationParameter authorizationParameter : authorizationParameters)
        {
            boolean found = false;
            for (AuthorizationParameter parameterBinding : parameterBindings)
            {
                if (parameterBinding.matches(authorizationParameter))
                {
                    found = true;
                }
            }
            if (!found)
            {
                return false;
            }
        }
        return true;
    }

    Method getBoundAuthorizerMethod()
    {
        return boundAuthorizerMethod.getJavaMember();
    }
}
