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

import javax.enterprise.inject.Typed;

@Typed()
public abstract class StringUtils
{
    /**
     * Constructor which prevents the instantiation of this class
     */
    private StringUtils()
    {
        // prevent instantiation
    }

    public static boolean isEmpty(String string)
    {
        return string == null || string.trim().isEmpty();
    }

    public static boolean isNotEmpty(String text)
    {
        return !isEmpty(text);
    }

    /**
     * Remove any non-numeric, non-alphanumeric Characters in the given String
     * @param val
     * @return the original string but any non-numeric, non-alphanumeric is replaced with a '_'
     */
    public static String removeSpecialChars(String val)
    {
        if (val == null)
        {
            return null;
        }

        int len = val.length();
        char[] newBuf = new char[len];
        val.getChars(0, len, newBuf, 0);
        for (int i = 0; i < len; i++)
        {
            char c = newBuf[i];
            if (c >= 'a' && c <= 'z' ||
                c >= 'A' && c <= 'Z' ||
                c >= '0' && c <= '9' ||
                c == '-' ||
                c == '_')
            {
                continue;
            }

            // every other char gets replaced with '_'
            newBuf[i] = '_';
        }

        return new String(newBuf);
    }

}