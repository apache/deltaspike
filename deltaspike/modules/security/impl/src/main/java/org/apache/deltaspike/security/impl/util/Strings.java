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
package org.apache.deltaspike.security.impl.util;

/**
 * String utility methods
 */
public class Strings 
{
    private Strings() 
    {
        
    }
    
    public static String unqualify(String name) 
    {
        return unqualify(name, '.');
    }

    public static String unqualify(String name, char sep) 
    {
        return name.substring(name.lastIndexOf(sep) + 1, name.length());
    }

    public static boolean isEmpty(String string) 
    {
        int len = string.length();
        if (string == null || len == 0) 
        {
            return true;
        }

        for (int i = 0; i < len; i++) 
        {
            if ((Character.isWhitespace(string.charAt(i)) == false)) 
            {
                return false;
            }
        }
        return true;
    }

    public static String toClassNameString(String sep, Object... objects) 
    {
        if (objects.length == 0) 
        {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) 
        {
            builder.append(sep);
            if (object == null) 
            {
                builder.append("null");
            } 
            else 
            {
                builder.append(object.getClass().getName());
            }
        }
        return builder.substring(sep.length());
    }

    public static String toString(Object... objects) 
    {
        return toString(" ", objects);
    }

    public static String toString(String sep, Object... objects) 
    {
        if (objects.length == 0) 
        {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) 
        {
            builder.append(sep).append(object);
        }
        return builder.substring(sep.length());
    }
}