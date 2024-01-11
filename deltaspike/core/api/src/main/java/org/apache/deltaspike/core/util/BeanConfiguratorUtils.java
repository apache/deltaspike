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
package org.apache.deltaspike.core.util;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;
import jakarta.inject.Named;

import java.beans.Introspector;
import java.lang.annotation.Annotation;


public class BeanConfiguratorUtils
{
    private BeanConfiguratorUtils()
    {
    }

    public static <T> BeanConfigurator<T> read(BeanManager beanManager,
                                           BeanConfigurator<T> beanConfigurator,
                                           AnnotatedType<T> type)
    {
        // Weld doesnt support interfaces...
        if (!type.getJavaClass().isInterface())
        {
            return beanConfigurator.read(type);
        }

        boolean qualifierAdded = false;

        for (Annotation annotation : type.getAnnotations())
        {
            if (beanManager.isQualifier(annotation.annotationType()))
            {
                beanConfigurator.addQualifier(annotation);
                qualifierAdded = true;
            }
            else if (beanManager.isScope(annotation.annotationType()))
            {
                beanConfigurator.scope(annotation.annotationType());
            }
            else if (beanManager.isStereotype(annotation.annotationType()))
            {
                beanConfigurator.addStereotype(annotation.annotationType());
            }
            if (annotation instanceof Named)
            {
                String name = ((Named) annotation).value();
                if (name == null || name.isBlank())
                {
                    name = Introspector.decapitalize(type.getJavaClass().getSimpleName());
                }
                beanConfigurator.name(name);
            }
        }

        if (type.isAnnotationPresent(Typed.class))
        {
            Typed typed = type.getAnnotation(Typed.class);
            beanConfigurator.types(typed.value());
        }
        else
        {
            for (Class<?> c = type.getJavaClass(); c != Object.class && c != null; c = c.getSuperclass())
            {
                beanConfigurator.addTypes(c);
            }
            beanConfigurator.addTypes(type.getJavaClass().getInterfaces());
            beanConfigurator.addTypes(Object.class);
        }

        if (!qualifierAdded)
        {
            beanConfigurator.addQualifier(Default.Literal.INSTANCE);
        }
        beanConfigurator.addQualifier(Any.Literal.INSTANCE);

        return beanConfigurator;
    }


}
