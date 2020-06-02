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
package org.apache.deltaspike.test.core.api.config.propertyconfigsource;

import javax.inject.Inject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.impl.config.PropertyFileConfigSource;
import org.junit.Assert;
import org.junit.Test;

public class BaseTestConfigProperty
{
    protected final static String CONFIG_FILE_NAME = "myconfig.properties";
    protected static final String CONFIG_VALUE = "deltaspike.dynamic.reloadable.config.value";

    @Inject
    private MyBean myBean;

    @Test
    public void testInjectConfig()
    {
        Assert.assertEquals("psAwareStringValue", myBean.getStringConfig());
        Assert.assertEquals("DEFAULT", myBean.getStringConfigWithDefault());
        Assert.assertEquals("stringValue", myBean.getStringConfigWithoutProjectStage());
        Assert.assertEquals("parameterizedPsAwareStringValue", myBean.getStringConfigParameterized());

        Assert.assertEquals(false, myBean.getBooleanConfig());
        Assert.assertEquals(MyBean.class, myBean.getClassConfig());
        Assert.assertEquals(5, myBean.getIntConfig());
        Assert.assertEquals(8589934592l, myBean.getLongConfig());
        Assert.assertEquals(-1.1f, myBean.getFloatConfig(), 0);
        Assert.assertEquals(4e40, myBean.getDoubleConfig(), 0);
    }

    @Test
    public void testDynamicReload() throws Exception
    {
        File prop = File.createTempFile("deltaspike-test", ".properties");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(prop)))
        {
            bw.write(CONFIG_VALUE + "=1\ndeltaspike_reload=1\n");
            bw.flush();
        }
        prop.deleteOnExit();

        final PropertyFileConfigSource dynamicReloadConfigSource = new PropertyFileConfigSource(prop.toURI().toURL());
        ConfigResolver.addConfigSources(Collections.singletonList(dynamicReloadConfigSource));

        Assert.assertEquals("1", ConfigResolver.getPropertyValue(CONFIG_VALUE));

        // we need to take care of file system granularity
        Thread.sleep(2100L);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(prop)))
        {
            bw.write(CONFIG_VALUE + "=2\ndeltaspike_reload=1\n");
            bw.flush();
        }

        Assert.assertEquals("2", ConfigResolver.getPropertyValue(CONFIG_VALUE));
    }
}
