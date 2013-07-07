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
package org.apache.deltaspike.data.impl.property;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

/**
 * A bean property based on the value contained in a field
 */
class FieldPropertyImpl<V> implements FieldProperty<V>
{

    private final Field field;

    FieldPropertyImpl(Field field)
    {
        this.field = field;
    }

    @Override
    public String getName()
    {
        return field.getName();
    }

    @Override
    public Type getBaseType()
    {
        return field.getGenericType();
    }

    @Override
    public Field getAnnotatedElement()
    {
        return field;
    }

    @Override
    public Member getMember()
    {
        return field;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<V> getJavaClass()
    {
        return (Class<V>) field.getType();
    }

    @Override
    public V getValue(Object instance)
    {
        setAccessible();
        return Reflections.getFieldValue(field, instance, getJavaClass());
    }

    @Override
    public void setValue(Object instance, V value)
    {
        setAccessible();
        Reflections.setFieldValue(true, field, instance, value);
    }

    @Override
    public Class<?> getDeclaringClass()
    {
        return field.getDeclaringClass();
    }

    @Override
    public boolean isReadOnly()
    {
        return false;
    }

    @Override
    public void setAccessible()
    {
        Reflections.setAccessible(field);
    }

    @Override
    public String toString()
    {
        return field.toString();
    }

    @Override
    public int hashCode()
    {
        return field.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return field.equals(obj);
    }
}
