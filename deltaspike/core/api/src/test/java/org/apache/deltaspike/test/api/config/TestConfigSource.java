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

import java.util.HashMap;
import java.util.Map;

import org.apache.deltaspike.core.spi.config.ConfigSource;

/**
 * Test ConfigSource
 *
 * It statically returns 'testvalue' for the key 'testkey'
 */
public class TestConfigSource implements ConfigSource
{

    private int ordinal = 700;

    private Map<String, String> props = new HashMap<String, String>();


    public TestConfigSource()
    {
        // a ProjectStage overloaded value
        props.put("testkey", "testvalue");
        props.put("testkey.UnitTest", "unittestvalue");

        // a value without any overloading
        props.put("testkey2", "testvalue");

        // a value which got ProjectStage overloaded to an empty value
        props.put("testkey3", "testvalue");
        props.put("testkey3.UnitTest", "");

        // now for the PropertyAware tests
        props.put("dbvendor.UnitTest", "mysql");
        props.put("dbvendor", "postgresql");

        props.put("dataSource.mysql.Production", "java:/comp/env/MyDs");
        props.put("dataSource.mysql.UnitTest", "TestDataSource");
        props.put("dataSource.postgresql", "PostgreDataSource");
        props.put("dataSource.UnitTest", "UnitTestDataSource");
        props.put("dataSource", "DefaultDataSource");

        // another one
        props.put("dbvendor2.Production", "mysql");
        props.put("dbvendor2", "postgresql");

        props.put("dbvendor3", "h2");

        props.put("testkey4.encrypted", "value");
        props.put("testkey4.password", "mysecretvalue");

        props.put("deltaspike.test.string-value", "configured");
        props.put("deltaspike.test.integer-value", "5");
        props.put("deltaspike.test.long-value", "8589934592");
        props.put("deltaspike.test.float-value", "-1.1");
        props.put("deltaspike.test.double-value", "4e40");
        props.put("deltaspike.test.boolean-value", Boolean.FALSE.toString());
        props.put("deltaspike.test.class-value", "org.apache.deltaspike.test.api.config.TestConfigSource");
        props.put("deltaspike.test.date-value", "2014-12-24");
        props.put("deltaspike.test.invalid-value", "wrong");
    }

    @Override
    public String getConfigName()
    {
        return "testConfig";
    }

    @Override
    public int getOrdinal()
    {
        return ordinal;
    }

    @Override
    public String getPropertyValue(String key)
    {
        return props.get(key);
    }

    @Override
    public Map<String, String> getProperties()
    {
        return props;
    }

	@Override
	public boolean isScannable() {
		return true;
	}

}
