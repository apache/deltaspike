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

import jakarta.enterprise.inject.Typed;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Utility to flatten nested Maps into a single level key:value pair set of properties.
 *
 * @since 2.0.1
 */
@Typed
public abstract class MapUtils
{
    /**
     * Don't construct this class. Only use the <code>static</code> methods.
     */
    private MapUtils()
    {
        // Do nothing
    }

    /**
     * Calls {@link #flattenMapProperties(Map, boolean)} with <code>indexed=false</code>.
     *
     * @param input Map of properties that may contain nested Maps.
     * @param <V> Type of values the {@link Map} contains.
     * @return Map of all properties indexed by their fully qualified names.
     * @see #flattenMapProperties(Map, boolean)
     */
    public static <V> Map<String, String> flattenMapProperties(final Map<String, V> input)
    {
        return flattenMapProperties(input, false);
    }

    /**
     * Converts a {@link Map} of objects to a flattened {@link Map} of {@link String} values.
     *
     * <p>For example, with the given input:</p>
     *
     * <pre><code>
     * Map&lt;String, Object&gt; application = Map.of(
     *     "name", "My App",
     *     "prefixes", List.of("&gt;", "$")
     * );
     *
     * Map&lt;String, Object&gt; map = Map.of("application", application);
     * Map&lt;String, String&gt; result = MapUtils.flattenMapProperties(map);
     * </code></pre>
     *
     * Will result in the following properties, assuming <code>indexed</code> is <code>false</code>:
     *
     * <pre><code>
     * application.name=My App
     * application.prefixes=&gt;,$
     * </code></pre>
     *
     * If <code>indexed</code> is <code>true</code>, the result would be:
     *
     * <pre><code>
     * application.name=My App
     * application.prefixes[0]=&gt;
     * application.prefixes[1]=$
     * </code></pre>
     *
     *
     * @param input Map of properties that may contain nested Maps.
     * @param indexed If arrays are converted to multiple properties, or a comma separated list.
     * @param <V> Type of values the {@link Map} contains.
     * @return Map of all properties indexed by their fully qualified names.
     */
    public static <V> Map<String, String> flattenMapProperties(final Map<String, V> input, final boolean indexed)
    {
        final Map<String, String> result = new HashMap<>();
        flattenMapProperties(input, result, indexed);
        return result;
    }

    /**
     * Calls {@link #flattenMapProperties(Map, Map, boolean, String)} with parameter <code>prefix</code> as <code>null</code>, since when we begin
     * flattening the map, there is no prefix by default.
     *
     * @param input Map of properties that may contain nested Maps.
     * @param output Map that all properties are added to.
     * @param indexed If arrays are converted to multiple properties, or a comma separated list.
     * @param <V> Type of values the {@link Map} contains.
     * @see #flattenMapProperties(Map, Map, boolean, String)
     */
    private static <V> void flattenMapProperties(final Map<String, V> input,
                                                 final Map<String, String> output,
                                                 final boolean indexed)
    {
        flattenMapProperties(input, output, indexed, null);
    }

    /**
     * @param input Map of properties that may contain nested Maps.
     * @param output Map that all properties are added to.
     * @param indexed If arrays are converted to multiple properties, or a comma separated list.
     * @param prefix Name to prefix to any properties found on this level.
     * @param <V> Type of values the {@link Map} contains.
     */
    private static <V> void flattenMapProperties(final Map<String, V> input,
                                                 final Map<String, String> output,
                                                 final boolean indexed,
                                                 final String prefix)
    {
        input.forEach((key, value) ->
            {
                if (value == null)
                {
                    return;
                }

                final String k = (prefix == null) ? key : (prefix + '.' + key);

                if (value instanceof Map)
                {
                    flattenMapProperties((Map) value, output, indexed, k);
                }
                else if (value instanceof Iterable)
                {
                    addIterable((Iterable) value, k, output, indexed);
                }
                else
                {
                    output.put(k, (output.containsKey(k)) ? output.get(k) + "," + value : value.toString());
                }
            });
    }

    /**
     * @param value Array of values that needs to be flattened.
     * @param key Property name for this value.
     * @param output Map that all properties are added to.
     * @param indexed If arrays are converted to multiple properties, or a comma separated list.
     * @param <V> Type of values the {@link Map} contains.
     */
    private static <V> void addIterable(final Iterable<V> value,
                                        final String key,
                                        final Map<String, String> output,
                                        final boolean indexed)
    {
        final StringJoiner joiner = new StringJoiner(",");
        int index = 0;

        for (final Object o : value)
        {
            if (o instanceof Map)
            {
                final Map map = (Map) o;

                if (map.isEmpty())
                {
                    continue;
                }

                final String keyPrefix = (indexed) ? key + "[" + index++ + "]" : key;
                flattenMapProperties((Map) map, output, indexed, keyPrefix);
            }
            else
            {
                joiner.add(o.toString());
            }
        }

        if (joiner.length() > 0)
        {
            output.put(key, joiner.toString());
        }
    }
}
