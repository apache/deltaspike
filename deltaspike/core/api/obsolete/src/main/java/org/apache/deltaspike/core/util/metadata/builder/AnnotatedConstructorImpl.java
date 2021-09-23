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

package org.apache.deltaspike.core.util.metadata.builder;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Implementation of {@link AnnotatedConstructor} to be used in
 * {@link org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder}
 * and other CDI life cycle events.
 */
class AnnotatedConstructorImpl<X> extends AnnotatedCallableImpl<X, Constructor<X>> implements AnnotatedConstructor<X>
{

    /**
     * Constructor
     */
    AnnotatedConstructorImpl(AnnotatedTypeImpl<X> type, Constructor<?> constructor, AnnotationStore annotations,
                                    Map<Integer, AnnotationStore> parameterAnnotations,
                                    Map<Integer, Type> typeOverrides)
    {

        super(type, (Constructor<X>) constructor, constructor.getDeclaringClass(), constructor.getParameterTypes(),
                getGenericArray(constructor), annotations, parameterAnnotations, null, typeOverrides);
    }

    private static Type[] getGenericArray(Constructor<?> constructor)
    {
        Type[] genericTypes = constructor.getGenericParameterTypes();
        // for inner classes genericTypes and parameterTypes can be different
        // length, this is a hack to fix this.
        // TODO: investigate this behavior further, on different JVM's and
        // compilers
        if (genericTypes.length < constructor.getParameterTypes().length)
        {
            genericTypes = new Type[constructor.getParameterTypes().length];
            genericTypes[0] = constructor.getParameterTypes()[0];
            for (int i = 0; i < constructor.getGenericParameterTypes().length; ++i)
            {
                genericTypes[i + 1] = constructor.getGenericParameterTypes()[i];
            }
        }
        return genericTypes;
    }

}
