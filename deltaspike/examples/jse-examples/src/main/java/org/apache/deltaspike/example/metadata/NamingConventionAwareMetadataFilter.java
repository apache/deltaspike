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
package org.apache.deltaspike.example.metadata;

import org.apache.deltaspike.core.api.literal.NamedLiteral;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Named;

/**
 * Just a test filter to show the basic functionality provided by {@link AnnotatedTypeBuilder}
 */
public class NamingConventionAwareMetadataFilter implements Extension
{
    public void ensureNamingConvention(@Observes ProcessAnnotatedType processAnnotatedType)
    {
        Class<?> beanClass = processAnnotatedType.getAnnotatedType().getJavaClass();

        Named namedAnnotation = beanClass.getAnnotation(Named.class);
        if (namedAnnotation != null &&
                namedAnnotation.value().length() > 0 &&
                Character.isUpperCase(namedAnnotation.value().charAt(0)))
        {
            AnnotatedTypeBuilder builder = new AnnotatedTypeBuilder();
            builder.readFromType(beanClass);

            String beanName = namedAnnotation.value();
            String newBeanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);

            builder.removeFromClass(Named.class)
                    .addToClass(new NamedLiteral(newBeanName));

            processAnnotatedType.setAnnotatedType(builder.create());
        }
    }
}
