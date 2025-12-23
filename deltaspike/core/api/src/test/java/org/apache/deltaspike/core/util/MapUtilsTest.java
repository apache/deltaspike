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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtilsTest
{
    /**
     * <pre><code>
     * application:
     *   name: MyApp
     * database:
     *   host: 127.0.0.1
     *   username: seth
     *   password: password
     * </code></pre>
     */
    @Test
    public void testFlattenMap()
    {
        Map<String, String> application = new HashMap<>();
        application.put("name", "MyApp");

        Map<String, String> database = new HashMap<>();
        database.put("host", "127.0.0.1");
        database.put("username", "username");
        database.put("password", "password");

        Map<String, Object> map = new HashMap<>();
        map.put("application", application);
        map.put("database", database);

        Map<String, String> result = MapUtils.flattenMapProperties(map);

        Assert.assertEquals(4, result.size());
        Assert.assertEquals("MyApp", result.get("application.name"));
        Assert.assertEquals("127.0.0.1", result.get("database.host"));
        Assert.assertEquals("username", result.get("database.username"));
        Assert.assertEquals("password", result.get("database.password"));
    }

    /**
     * <pre><code>
     * application:
     *   name: Another App
     *   database:
     *     host: localhost
     *     username: username
     *     password: password
     * </code></pre>
     */
    @Test
    public void testFlattenNestedMap()
    {
        Map<String, String> database = new HashMap<>();
        database.put("host", "localhost");
        database.put("username", "username");
        database.put("password", "password");

        Map<String, Object> application = new HashMap<>();
        application.put("name", "Another App");
        application.put("database", database);

        Map<String, Object> map = new HashMap<>();
        map.put("application", application);

        Map<String, String> result = MapUtils.flattenMapProperties(map);

        Assert.assertEquals(4, result.size());
        Assert.assertEquals("Another App", result.get("application.name"));
        Assert.assertEquals("localhost", result.get("application.database.host"));
        Assert.assertEquals("username", result.get("application.database.username"));
        Assert.assertEquals("password", result.get("application.database.password"));
    }

    /**
     * <pre><code>
     * application:
     *   name: Yet Another App
     *   prefixes:
     *     - >
     *     - $
     * </code></pre>
     */
    @Test
    public void testFlattenWithList()
    {
        List<String> prefixes = new ArrayList<>();
        prefixes.add(">");
        prefixes.add("$");

        Map<String, Object> application = new HashMap<>();
        application.put("name", "Yet Another App");
        application.put("prefixes", prefixes);

        Map<String, Object> map = new HashMap<>();
        map.put("application", application);

        Map<String, String> result = MapUtils.flattenMapProperties(map);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("Yet Another App", result.get("application.name"));
        Assert.assertEquals(">,$", result.get("application.prefixes"));
    }

    /**
     * <pre><code>
     * application:
     *   name: More Apps
     *   messages:
     *     - source: one
     *       target: two
     *     - source: three
     *       target: four
     * </code></pre>
     */
    @Test
    public void testFlattenWithObjectArrayLists()
    {
        Map<String, String> message1 = new HashMap<>();
        message1.put("source", "one");
        message1.put("target", "two");

        Map<String, String> message2 = new HashMap<>();
        message2.put("source", "three");
        message2.put("target", "four");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);

        Map<String, Object> application = new HashMap<>();
        application.put("name", "More Apps");
        application.put("messages", messages);

        Map<String, Object> map = new HashMap<>();
        map.put("application", application);

        Map<String, String> result = MapUtils.flattenMapProperties(map);

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("More Apps", result.get("application.name"));
        Assert.assertEquals("one,three", result.get("application.messages.source"));
        Assert.assertEquals("two,four", result.get("application.messages.target"));
    }

    /**
     * Ignore nested null values.
     *
     * <pre><code>
     * application:
     *   name: More Apps
     *   messages:
     *     - source: one
     *       target: two
     *     - source: null
     *       target: null
     *     - source: three
     *       target: four
     * </code></pre>
     */
    @Test
    public void testFlattenWithObjectArrayListsWithNull()
    {
        Map<String, String> message1 = new HashMap<>();
        message1.put("source", "one");
        message1.put("target", "two");

        Map<String, String> message2 = new HashMap<>();
        message2.put("source", null);
        message2.put("target", null);

        Map<String, String> message3 = new HashMap<>();
        message2.put("source", "three");
        message2.put("target", "four");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);
        messages.add(message3);

        Map<String, Object> application = new HashMap<>();
        application.put("name", "More Apps");
        application.put("messages", messages);

        Map<String, Object> map = new HashMap<>();
        map.put("application", application);

        Map<String, String> result = MapUtils.flattenMapProperties(map);

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("More Apps", result.get("application.name"));
        Assert.assertEquals("one,three", result.get("application.messages.source"));
        Assert.assertEquals("two,four", result.get("application.messages.target"));
    }

    /**
     * <pre><code>
     * application:
     *   name: More Apps
     *   messages:
     *     - source: one
     *       target: two
     *     - source: three
     *       target: four
     * </code></pre>
     */
    @Test
    public void testFlattenWithObjectArrayIndexed()
    {
        Map<String, String> message1 = new HashMap<>();
        message1.put("source", "one");
        message1.put("target", "two");

        Map<String, String> message2 = new HashMap<>();
        message2.put("source", "three");
        message2.put("target", "four");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);

        Map<String, Object> application = new HashMap<>();
        application.put("name", "More Apps");
        application.put("messages", messages);

        Map<String, Object> map = new HashMap<>();
        map.put("application", application);

        Map<String, String> result = MapUtils.flattenMapProperties(map, true);

        Assert.assertEquals(5, result.size());
        Assert.assertEquals("More Apps", result.get("application.name"));
        Assert.assertEquals("one", result.get("application.messages[0].source"));
        Assert.assertEquals("two", result.get("application.messages[0].target"));
        Assert.assertEquals("three", result.get("application.messages[1].source"));
        Assert.assertEquals("four", result.get("application.messages[1].target"));
    }

    /**
     * Ignore nested null values.
     *
     * <pre><code>
     * application:
     *   name: More Apps
     *   messages:
     *     - source: one
     *       target: two
     *     - source: null
     *       target: null
     *     - source: three
     *       target: four
     * </code></pre>
     */
    @Test
    public void testFlattenWithObjectArrayIndexedWithNull()
    {
        Map<String, String> message1 = new HashMap<>();
        message1.put("source", "one");
        message1.put("target", "two");

        Map<String, String> message2 = new HashMap<>();
        message2.put("source", "three");
        message2.put("target", "four");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);

        Map<String, Object> application = new HashMap<>();
        application.put("name", "More Apps");
        application.put("messages", messages);

        Map<String, Object> map = new HashMap<>();
        map.put("application", application);

        Map<String, String> result = MapUtils.flattenMapProperties(map, true);

        Assert.assertEquals(5, result.size());
        Assert.assertEquals("More Apps", result.get("application.name"));
        Assert.assertEquals("one", result.get("application.messages[0].source"));
        Assert.assertEquals("two", result.get("application.messages[0].target"));
        Assert.assertEquals("three", result.get("application.messages[1].source"));
        Assert.assertEquals("four", result.get("application.messages[1].target"));
    }

    /**
     * <pre><code>
     * application:
     *   name: Null App
     *   prefixes:
     * </code></pre>
     */
    @Test
    public void testWithNull()
    {
        Map<String, Object> application = new HashMap<>();
        application.put("name", "Null App");
        application.put("prefixes", null);

        Map<String, Object> map = new HashMap<>();
        map.put("application", application);

        Map<String, String> result = MapUtils.flattenMapProperties(map);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("Null App", result.get("application.name"));
    }
}
