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

public class MethodPrefix
{
    public static final String DEFAULT_PREFIX = "findBy";
    public static final String DEFAULT_OPT_PREFIX = "findOptionalBy";
    public static final String DEFAULT_ANY_PREFIX = "findAnyBy";
    public static final String DEFAULT_DELETE_PREFIX = "deleteBy";
    public static final String DEFAULT_REMOVE_PREFIX = "removeBy";

    private final String customPrefix;
    private final String methodName;

    public MethodPrefix(String customPrefix, String methodName)
    {
        this.customPrefix = customPrefix;
        this.methodName = methodName;
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

    private static enum KnownQueryPrefix
    {
        DEFAULT(DEFAULT_PREFIX)
        {
            @Override
            public SingleResultType getStyle()
            {
                return SingleResultType.JPA;
            }
        },
        OPTIONAL(DEFAULT_OPT_PREFIX)
        {
            @Override
            public SingleResultType getStyle()
            {
                return SingleResultType.OPTIONAL;
            }
        },
        ANY(DEFAULT_ANY_PREFIX)
        {
            @Override
            public SingleResultType getStyle()
            {
                return SingleResultType.ANY;
            }
        },
        DELETE_DEFAULT(DEFAULT_DELETE_PREFIX)
        {
            @Override
            public SingleResultType getStyle()
            {
                return SingleResultType.ANY;
            }
        },
        REMOVE_DEFAULT(DEFAULT_REMOVE_PREFIX)
        {
            @Override
            public SingleResultType getStyle()
            {
                return SingleResultType.ANY;
            }
        };

        private final String prefix;

        private KnownQueryPrefix(String prefix)
        {
            this.prefix = prefix;
        }

        public String removePrefix(String queryPart)
        {
            return queryPart.substring(prefix.length());
        }

        public String getPrefix()
        {
            return prefix;
        }

        public abstract SingleResultType getStyle();

        public static KnownQueryPrefix fromMethodName(String name)
        {
            for (KnownQueryPrefix mapping : values())
            {
                if (name.startsWith(mapping.getPrefix()))
                {
                    return mapping;
                }
            }
            return null;
        }
    }

}
