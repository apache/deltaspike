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

package org.apache.deltaspike.core.util.metadata;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Simple wrapper for injection points. Some metadata (as of 2014-12-15 just Bean) can be overridden, all else
 * delegates to the wrapped InjectionPoint.
 */
public class InjectionPointWrapper implements InjectionPoint
{
    private final InjectionPoint wrapped;
    private final Bean<?> newBean;

    public InjectionPointWrapper(InjectionPoint wrapped, Bean<?> newBean)
    {
        this.wrapped = wrapped;
        this.newBean = newBean;
    }

    @Override
    public Type getType()
    {
        return wrapped.getType();
    }

    @Override
    public Set<Annotation> getQualifiers()
    {
        return wrapped.getQualifiers();
    }

    @Override
    public Bean<?> getBean()
    {
        return newBean;
    }

    @Override
    public Member getMember()
    {
        return wrapped.getMember();
    }

    @Override
    public Annotated getAnnotated()
    {
        return wrapped.getAnnotated();
    }

    @Override
    public boolean isDelegate()
    {
        return wrapped.isDelegate();
    }

    @Override
    public boolean isTransient()
    {
        return wrapped.isTransient();
    }
}
