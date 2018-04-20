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
package org.apache.deltaspike.test.core.api.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.deltaspike.core.api.config.Config;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.ConfigSnapshot;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ConfigSnapshotTest
{
    private static final String HOST_KEY = "ds.test.myapp.host";
    private static final String PORT1_KEY = "ds.test.myapp.port1";
    private static final String PORT2_KEY = "ds.test.myapp.port2";

    private ConfigResolver.TypedResolver<String> hostCfg;
    private ConfigResolver.TypedResolver<Integer> port1Cfg;
    private ConfigResolver.TypedResolver<Integer> port2Cfg;


    @Test
    public void testConfigTx()
    {
        Config cfg = ConfigResolver.getConfig();
        ConfigurableTestConfigSource configSource = ConfigurableTestConfigSource.instance();
        try
        {
            Map<String, String> newVals = new HashMap<>();
            newVals.put(HOST_KEY, "host1");
            newVals.put(PORT1_KEY, "1");
            newVals.put(PORT2_KEY, "1");
            configSource.setValues(newVals);

            hostCfg = cfg.resolve(HOST_KEY);
            port1Cfg = cfg.resolve(PORT1_KEY).as(Integer.class);
            port2Cfg = cfg.resolve(PORT2_KEY).as(Integer.class);

            assertEquals("host1", hostCfg.getValue());
            assertEquals(Integer.valueOf(1), port1Cfg.getValue());
            assertEquals(Integer.valueOf(1), port2Cfg.getValue());

            ConfigSnapshot configSnapshot = cfg.snapshotFor(hostCfg, port1Cfg, port2Cfg);
            assertNotNull(configSnapshot);

            assertEquals("host1", hostCfg.getValue(configSnapshot));
            assertEquals(Integer.valueOf(1), port1Cfg.getValue(configSnapshot));
            assertEquals(Integer.valueOf(1), port2Cfg.getValue(configSnapshot));

            // and those values don't change, even if we modify the underlying ConfigSource!
            newVals.clear();
            newVals.put(HOST_KEY, "host2");
            newVals.put(PORT1_KEY, "2");
            newVals.put(PORT2_KEY, "2");
            configSource.setValues(newVals);

            assertEquals("host1", hostCfg.getValue(configSnapshot));
            assertEquals(Integer.valueOf(1), port1Cfg.getValue(configSnapshot));
            assertEquals(Integer.valueOf(1), port2Cfg.getValue(configSnapshot));

            // but the actual config did really change!
            assertEquals("host2", hostCfg.getValue());
            assertEquals(Integer.valueOf(2), port1Cfg.getValue());
            assertEquals(Integer.valueOf(2), port2Cfg.getValue());
        }
        finally
        {
            configSource.clear();
        }
    }


}
