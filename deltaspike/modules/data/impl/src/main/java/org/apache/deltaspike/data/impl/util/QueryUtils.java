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
package org.apache.deltaspike.data.impl.util;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collection;

/**
 *
 * @author thomashug
 */
public final class QueryUtils
{
    private static final String KEYWORD_SPLITTER = "({0})(?=[A-Z])";

    private QueryUtils()
    {
    }

    public static String[] splitByKeyword(String query, String keyword)
    {
        return query.split(MessageFormat.format(KEYWORD_SPLITTER, keyword));
    }

    public static String uncapitalize(String value)
    {
        if (isEmpty(value))
        {
            return null;
        }
        if (value.length() == 1)
        {
            return value.toLowerCase();
        }
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    public static boolean isEmpty(String text)
    {
        return text == null || "".equals(text);
    }

    public static boolean isNotEmpty(String text)
    {
        return !isEmpty(text);
    }

    public static boolean isEmpty(Collection<?> collection)
    {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Object[] array)
    {
        return array == null || array.length == 0;
    }

    public static boolean isString(Object value)
    {
        return value != null && value instanceof String;
    }

    public static boolean contains(Class<?> clazz, Method method)
    {
        return extract(clazz, method) != null;
    }

    public static Method extract(Class<?> clazz, Method method)
    {
        try
        {
            String name = method.getName();
            return clazz.getMethod(name, method.getParameterTypes());
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }
}
