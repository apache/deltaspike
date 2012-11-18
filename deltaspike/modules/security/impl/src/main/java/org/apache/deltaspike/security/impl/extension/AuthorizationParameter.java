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
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.util.Nonbinding;

import org.apache.deltaspike.security.api.authorization.SecurityDefinitionException;

class AuthorizationParameter
{

    private Type type;
    private Map<Class<? extends Annotation>, Map<Method, Object>> bindings;

    public AuthorizationParameter()
    {
    }

    AuthorizationParameter(Type type, Set<Annotation> bindings)
    {
        this.type = type;
        this.bindings = new HashMap<Class<? extends Annotation>, Map<Method, Object>>();
        for (Annotation bindingAnnotation : bindings)
        {
            Map<Method, Object> bindingMembers = new HashMap<Method, Object>();
            try
            {
                for (Method method : bindingAnnotation.annotationType().getDeclaredMethods())
                {
                    if (method.isAnnotationPresent(Nonbinding.class))
                    {
                        continue;
                    }
                    bindingMembers.put(method, method.invoke(bindingAnnotation));
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
            this.bindings.put(bindingAnnotation.annotationType(), bindingMembers);
        }
    }

    /**
     * TODO comment is no equals!!!
     * 
     * @param parameter
     * @return
     */
    boolean matches(AuthorizationParameter parameter)
    {
        if (!type.equals(parameter.type))
        {
            return false;
        }
        for (Map.Entry<Class<? extends Annotation>, Map<Method, Object>> bindingEntry : bindings.entrySet())
        {
            Map<Method, Object> bindingValues = parameter.bindings.get(bindingEntry.getKey());
            if (bindingValues == null)
            {
                // annotation is not present
                return false;
            }
            for (Map.Entry<Method, Object> value : bindingEntry.getValue().entrySet())
            {
                if (!bindingValues.get(value.getKey()).equals(value.getValue()))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Class<? extends Annotation>, Map<Method, Object>> bindingEntry : bindings.entrySet())
        {
            builder.append('@').append(bindingEntry.getKey().getName()).append('(');
            for (Map.Entry<Method, Object> value : bindingEntry.getValue().entrySet())
            {
                builder.append(value.getKey().getName()).append('=').append(value.getValue()).append(',');
            }
            if (bindingEntry.getValue().isEmpty())
            {
                builder.append(')');
            }
            else
            {
                builder.setCharAt(builder.length() - 1, ')');
            }
        }
        if (!bindings.isEmpty())
        {
            builder.append(' ');
        }
        builder.append(type);
        return builder.toString();
    }
}
