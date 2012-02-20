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
package org.apache.deltaspike.test.api.config;

import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * ConfigSourceProvider for basic unit-test
 */
public class TestConfigSourceProvider implements ConfigSourceProvider
{
    @Override
    public List<ConfigSource> getConfigSources()
    {
        return new ArrayList<ConfigSource>()
        {
            {
                add(new ConfigSource()
                {
                    @Override
                    public int getOrdinal()
                    {
                        return 1;
                    }

                    @Override
                    public String getPropertyValue(String key)
                    {
                        if ("test".equals(key))
                        {
                            return "test1";
                        }
                        return null;
                    }

                    @Override
                    public String getConfigName()
                    {
                        return TestConfigSourceProvider.class.getName() + "-1";
                    }
                });
                add(new ConfigSource()
                {
                    @Override
                    public int getOrdinal()
                    {
                        return 2;
                    }

                    @Override
                    public String getPropertyValue(String key)
                    {
                        if ("test".equals(key))
                        {
                            return "test2";
                        }
                        return null;
                    }

                    @Override
                    public String getConfigName()
                    {
                        return TestConfigSourceProvider.class.getName() + "-2";
                    }
                });
            }
        };
    }
}
