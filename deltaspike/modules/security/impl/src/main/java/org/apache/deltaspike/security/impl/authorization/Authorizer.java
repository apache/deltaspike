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

import org.apache.deltaspike.core.api.metadata.builder.InjectableMethod;
import org.apache.deltaspike.core.api.metadata.builder.ParameterValueRedefiner;
import org.apache.deltaspike.security.api.authorization.AuthorizationException;
import org.apache.deltaspike.security.api.authorization.SecurityDefinitionException;
import org.apache.deltaspike.security.api.authorization.annotation.SecurityBindingType;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Authorizer
{
    private BeanManager beanManager;

    private Annotation binding;
    private Map<Method, Object> memberValues = new HashMap<Method, Object>();

    private AnnotatedMethod<?> implementationMethod;
    private Bean<?> targetBean;

    private InjectableMethod<?> injectableMethod;

    Authorizer(Annotation binding, AnnotatedMethod<?> implementationMethod, BeanManager beanManager)
    {
        this.binding = binding;
        this.implementationMethod = implementationMethod;
        this.beanManager = beanManager;

        try
        {
            for (Method method : binding.annotationType().getDeclaredMethods())
            {
                if (method.isAnnotationPresent(Nonbinding.class))
                {
                    continue;
                }
                memberValues.put(method, method.invoke(binding));
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

    public void authorize(final InvocationContext ic)
    {
        if (targetBean == null)
        {
            lazyInitTargetBean();
        }

        final CreationalContext<?> creationalContext = beanManager.createCreationalContext(targetBean);

        Object reference = beanManager.getReference(targetBean,
            implementationMethod.getJavaMember().getDeclaringClass(), creationalContext);

        Object result = injectableMethod.invoke(reference, creationalContext, new ParameterValueRedefiner() {

            @Override
            public Object redefineParameterValue(ParameterValue value)
            {
                if (value.getInjectionPoint().getAnnotated().getBaseType().equals(InvocationContext.class))
                {
                    return ic;
                }
                else
                {
                    return value.getDefaultValue(creationalContext);
                }
            }
        });

        if (result.equals(Boolean.FALSE))
        {
            throw new AuthorizationException("Authorization check failed");
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private synchronized void lazyInitTargetBean()
    {
        if (targetBean == null)
        {
            Method method = implementationMethod.getJavaMember();

            Set<Bean<?>> beans = beanManager.getBeans(method.getDeclaringClass());
            if (beans.size() == 1)
            {
                targetBean = beans.iterator().next();
            }
            else if (beans.isEmpty())
            {
                throw new IllegalStateException("Exception looking up authorizer method bean - " +
                        "no beans found for method [" + method.getDeclaringClass() + "." +
                        method.getName() + "]");
            }
            else if (beans.size() > 1)
            {
                throw new IllegalStateException("Exception looking up authorizer method bean - " +
                        "multiple beans found for method [" + method.getDeclaringClass().getName() + "." +
                        method.getName() + "]");
            }

            injectableMethod = new InjectableMethod(implementationMethod, targetBean, beanManager);
        }
    }

    public boolean matchesBinding(Annotation annotation)
    {
        if (!annotation.annotationType().isAnnotationPresent(SecurityBindingType.class) &&
                annotation.annotationType().isAnnotationPresent(Stereotype.class))
        {
            annotation = SecurityUtils.resolveSecurityBindingType(annotation);
        }

        if (!annotation.annotationType().equals(binding.annotationType()))
        {
            return false;
        }

        for (Method method : annotation.annotationType().getDeclaredMethods())
        {
            if (method.isAnnotationPresent(Nonbinding.class))
            {
                continue;
            }

            if (!memberValues.containsKey(method))
            {
                return false;
            }

            try
            {
                Object value = method.invoke(annotation);
                if (!memberValues.get(method).equals(value))
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

        return true;
    }

    public Method getImplementationMethod()
    {
        return implementationMethod.getJavaMember();
    }

    @Override
    public boolean equals(Object value)
    {
        return false;
    }

    //not used
    @Override
    public int hashCode()
    {
        return 0;
    }
}
