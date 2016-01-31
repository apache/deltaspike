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

import java.text.MessageFormat;
import org.apache.deltaspike.core.util.StringUtils;

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
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        if (value.length() == 1)
        {
            return value.toLowerCase();
        }
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    public static boolean isString(Object value)
    {
        return value != null && value instanceof String;
    }

    public static String nullSafeValue(String value)
    {
        return nullSafeValue(value, null);
    }

    public static String nullSafeValue(String value, String fallback)
    {
        return value != null ? value : (fallback != null ? fallback : "");
    }

}
