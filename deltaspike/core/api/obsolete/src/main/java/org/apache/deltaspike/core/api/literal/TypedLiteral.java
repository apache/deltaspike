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

package org.apache.deltaspike.core.api.literal;

import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.util.AnnotationLiteral;

/**
 * Literal for {@link jakarta.enterprise.inject.Typed}
 */
public class TypedLiteral extends AnnotationLiteral<Typed> implements Typed
{
    private static final long serialVersionUID = 6805980497117269525L;

    private final Class<?>[] value;

    public TypedLiteral()
    {
        value = new Class<?>[0];
    }

    public TypedLiteral(Class<?>[] value)
    {
        this.value = value;
    }

    @Override public Class<?>[] value()
    {
        return value;
    }
}
