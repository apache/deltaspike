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
package org.apache.deltaspike.jsf.impl.util;

import javax.enterprise.inject.Typed;
import javax.faces.context.FacesContext;

@Typed()
public abstract class SharedStringBuilder
{
    /**
     * Get a shared {@link StringBuilder} instance.
     *
     * @param context The {@link FacesContext}
     * @param key The unique key per use case.
     * @return The shared {@link StringBuilder} instance
     */
    public static StringBuilder get(FacesContext context, String key)
    {
        StringBuilder builder = (StringBuilder) context.getAttributes().get(key);

        if (builder == null)
        {
            builder = new StringBuilder();
            context.getAttributes().put(key, builder);
        }
        else
        {
            builder.setLength(0);
        }

        return builder;
    }

    /**
     * Get a shared {@link StringBuilder} instance.
     *
     * @param key The unique key per use case.
     * @return The shared {@link StringBuilder} instance
     */
    public static StringBuilder get(String key)
    {
        return get(FacesContext.getCurrentInstance(), key);
    }
}
