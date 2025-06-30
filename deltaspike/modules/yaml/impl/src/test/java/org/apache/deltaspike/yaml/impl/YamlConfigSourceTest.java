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
package org.apache.deltaspike.yaml.impl;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class YamlConfigSourceTest
{
    /**
     * It should <strong>not</strong> throw an error in the case this file does not exist.
     */
    @Test
    public void exceptionNotThrownOnDefaultConfiguration()
    {
        try
        {
            new YamlConfigSource();
        }
        catch (Exception ex)
        {
            Assert.fail();
        }
    }

    @Test
    public void testThatInputStreamWorks() throws IOException
    {
        String yaml =
            "application:\n"    +
            "  name: Testing";

        try (InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)))
        {
            YamlConfigSource config = new YamlConfigSource(stream);

            Assert.assertEquals("yaml input-stream", config.getConfigName());
            Assert.assertFalse(config.isIndexed());
            Assert.assertEquals("Testing", config.getPropertyValue("application.name"));
        }
    }

    @Test
    public void testInputStreamWithCustomName() throws IOException
    {
        String yaml =
            "application:\n"    +
            "  name: Testing";

        try (InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)))
        {
            YamlConfigSource config = new YamlConfigSource(stream, "custom-stream", true);

            Assert.assertEquals("yaml custom-stream", config.getConfigName());
            Assert.assertTrue(config.isIndexed());
            Assert.assertEquals("Testing", config.getPropertyValue("application.name"));
        }
    }

    @Test
    public void testWithNullInputStream()
    {
        try
        {
            new YamlConfigSource((InputStream)null);
        }
        catch (Exception ex)
        {
            Assert.fail();
        }
    }
}
