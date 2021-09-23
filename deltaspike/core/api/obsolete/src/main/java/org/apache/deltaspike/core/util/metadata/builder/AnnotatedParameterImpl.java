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

import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import java.lang.reflect.Type;

/**
 * Implementation of {@link AnnotatedParameter}.
 */
class AnnotatedParameterImpl<X> extends AnnotatedImpl implements AnnotatedParameter<X>
{

    private final int position;
    private final AnnotatedCallable<X> declaringCallable;

    /**
     * Constructor
     */
    AnnotatedParameterImpl(AnnotatedCallable<X> declaringCallable, Class<?> type, int position,
                                  AnnotationStore annotations, Type genericType, Type typeOverride)
    {
        super(type, annotations, genericType, typeOverride);
        this.declaringCallable = declaringCallable;
        this.position = position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotatedCallable<X> getDeclaringCallable()
    {
        return declaringCallable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPosition()
    {
        return position;
    }

}
