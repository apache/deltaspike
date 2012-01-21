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

import javax.enterprise.inject.New;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal for {@link New}
 */
public class NewLiteral extends AnnotationLiteral<New> implements New
{
    private static final long serialVersionUID = -4134892777333672942L;

    private final Class<?> value;

    public NewLiteral()
    {
        this(New.class);
    }

    public NewLiteral(Class<?> value)
    {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> value()
    {
        return value;
    }
}
