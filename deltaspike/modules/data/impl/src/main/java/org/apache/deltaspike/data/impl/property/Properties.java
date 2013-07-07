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
import java.lang.reflect.Method;

/**
 * Utility class for working with JavaBean style properties
 *
 * @see Property
 */
public class Properties
{

    private Properties()
    {
    }

    /**
     * Create a JavaBean style property from the field
     *
     * @param <V>
     * @param field
     * @return
     */
    public static <V> FieldProperty<V> createProperty(Field field)
    {
        return new FieldPropertyImpl<V>(field);
    }

    /**
     * Create a JavaBean style property from the specified method
     *
     * @param <V>
     * @param method
     * @return
     * @throws IllegalArgumentException
     *             if the method does not match JavaBean conventions
     * @see http://www.oracle.com/technetwork/java/javase/documentation/spec-136004.html
     */
    public static <V> MethodProperty<V> createProperty(Method method)
    {
        return new MethodPropertyImpl<V>(method);
    }

    /**
     * Create a JavaBean style property from the specified member
     *
     * @param <V>
     * @param member
     * @return
     * @throws IllegalArgumentException
     *             if the method does not match JavaBean conventions
     * @see http://www.oracle.com/technetwork/java/javase/documentation/spec-136004.html
     */
    public static <V> Property<V> createProperty(Member member)
    {
        if (member instanceof Method)
        {
            return new MethodPropertyImpl<V>(Method.class.cast(member));
        }
        else if (member instanceof Field)
        {
            return new FieldPropertyImpl<V>(Field.class.cast(member));
        }
        else
        {
            throw new IllegalArgumentException("Cannot make a property of " + member
                    + " - it is neither a method or a field");
        }
    }

    /**
     * Indicates whether this method is a valid property method.
     */
    public static <V> boolean isProperty(Method method)
    {
        try
        {
            new MethodPropertyImpl<V>(method);
            return true;
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }
}
