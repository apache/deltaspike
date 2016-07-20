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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.inject.Typed;

@Typed()
public abstract class ParameterUtil
{
    private static boolean parameterSupported = true;
    private static Class<?> parameterClass;
    private static Method getNameMethod;
    private static Method getParametersMethod;

    static
    {
        try
        {
            parameterClass = Class.forName("java.lang.reflect.Parameter");
            getNameMethod = parameterClass.getMethod("getName");
            getParametersMethod = Method.class.getMethod("getParameters");
        }
        catch (Exception e)
        {
            parameterSupported = false;
            parameterClass = null;
            getNameMethod = null;
            getParametersMethod = null;
        }
    }

    public static boolean isParameterSupported()
    {
        return parameterSupported;
    }

    public static String getName(Method method, int parameterIndex)
    {
        if (!isParameterSupported() || method == null)
        {
            return null;
        }
        try
        {
            Object[] parameters = (Object[]) getParametersMethod.invoke(method);
            return (String) getNameMethod.invoke(parameters[parameterIndex]);
        }
        catch (IllegalAccessException e)
        {
        }
        catch (InvocationTargetException e)
        {
        }
        return null;
    }
}
