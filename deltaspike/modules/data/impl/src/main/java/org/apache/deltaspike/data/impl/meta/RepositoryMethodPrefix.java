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
package org.apache.deltaspike.data.impl.meta;

import org.apache.deltaspike.data.api.SingleResultType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepositoryMethodPrefix
{
    public static final String DEFAULT_PREFIX = "findBy";
    public static final String DEFAULT_OPT_PREFIX = "findOptionalBy";
    public static final String DEFAULT_ANY_PREFIX = "findAnyBy";
    public static final String DEFAULT_DELETE_PREFIX = "deleteBy";
    public static final String DEFAULT_COUNT_PREFIX = "countBy";
    public static final String DEFAULT_REMOVE_PREFIX = "removeBy";
    private static final String FIND_ALL_PREFIX = "findAll";
    
    private static final String FIND_FIRST_PREFIX = "find(First|Top)(\\d+)(By)*";
    private static final String FIND_FIRST_PREFIX_PATTERN = FIND_FIRST_PREFIX + "(.*)";
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");

    private final String customPrefix;
    private final String methodName;
    private int definedMaxResults = 0;

    public RepositoryMethodPrefix(String customPrefix, String methodName)
    {
        this.customPrefix = customPrefix;
        this.methodName = methodName;
        if (this.methodName != null)
        {
            this.parseMaxResults();
        }
    }

    public String removePrefix(String queryPart)
    {
        if (hasCustomPrefix() && queryPart.startsWith(customPrefix))
        {
            return queryPart.substring(customPrefix.length());
        }
        KnownQueryPrefix known = KnownQueryPrefix.fromMethodName(queryPart);
        if (known != null)
        {
            return known.removePrefix(queryPart);
        }
        return queryPart;
    }

    public boolean hasCustomPrefix()
    {
        return !"".equals(customPrefix);
    }

    public String getCustomPrefix()
    {
        return customPrefix;
    }

    public String getPrefix()
    {
        if (hasCustomPrefix())
        {
            return customPrefix;
        }
        KnownQueryPrefix prefix = KnownQueryPrefix.fromMethodName(methodName);
        if (prefix != null)
        {
            return prefix.getPrefix();
        }
        return "";
    }

    public SingleResultType getSingleResultStyle()
    {
        KnownQueryPrefix prefix = KnownQueryPrefix.fromMethodName(methodName);
        if (prefix != null)
        {
            return prefix.getStyle();
        }
        return SingleResultType.JPA;
    }

    public boolean isDelete()
    {
        return this.getPrefix().equalsIgnoreCase(DEFAULT_DELETE_PREFIX) ||
                this.getPrefix().equalsIgnoreCase(DEFAULT_REMOVE_PREFIX);
    }

    public boolean isCount()
    {
        return this.getPrefix().equalsIgnoreCase(DEFAULT_COUNT_PREFIX);
    }
    
    public int getDefinedMaxResults()
    {
        return definedMaxResults;
    }

    private void parseMaxResults()
    {
        if (this.methodName.matches(FIND_FIRST_PREFIX_PATTERN))
        {
            Matcher matcher = DIGIT_PATTERN.matcher(this.methodName);
            if (matcher.find())
            {
                this.definedMaxResults = Integer.parseInt(matcher.group());
            }
        }
    }

    private enum KnownQueryPrefix
    {
        DEFAULT(DEFAULT_PREFIX, SingleResultType.JPA),
        ALL(FIND_ALL_PREFIX, SingleResultType.JPA),
        FIND_FIRST(FIND_FIRST_PREFIX, SingleResultType.JPA)
        {
            @Override
            public boolean matches(String name)
            {
                return name.matches(FIND_FIRST_PREFIX_PATTERN);
            }
            @Override
            public String removePrefix(String queryPart)
            {
                return queryPart.replaceFirst(FIND_FIRST_PREFIX,"");
            }
        },
        OPTIONAL(DEFAULT_OPT_PREFIX,SingleResultType.OPTIONAL),
        ANY(DEFAULT_ANY_PREFIX, SingleResultType.ANY),
        DELETE_DEFAULT(DEFAULT_DELETE_PREFIX, SingleResultType.ANY),
        REMOVE_DEFAULT(DEFAULT_REMOVE_PREFIX, SingleResultType.ANY),
        COUNT_DEFAULT(DEFAULT_COUNT_PREFIX, SingleResultType.ANY);

        private final String prefix;
        private final SingleResultType singleResultType;

        KnownQueryPrefix(String prefix, SingleResultType singleResultType)
        {
            this.prefix = prefix;
            this.singleResultType = singleResultType;
        }

        public String removePrefix(String queryPart)
        {
            return queryPart.substring(prefix.length());
        }

        public String getPrefix()
        {
            return prefix;
        }

        public SingleResultType getStyle()
        {
            return this.singleResultType;
        }

        public boolean matches(String name)
        {
            return name.startsWith(getPrefix());
        }

        public static KnownQueryPrefix fromMethodName(String name)
        {
            for (KnownQueryPrefix mapping : values())
            {
                if (mapping.matches(name))
                {
                    return mapping;
                }
            }
            return null;
        }
    }

}
