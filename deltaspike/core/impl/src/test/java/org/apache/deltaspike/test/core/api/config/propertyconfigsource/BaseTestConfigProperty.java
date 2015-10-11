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

import org.junit.Assert;
import org.junit.Test;

public class BaseTestConfigProperty
{
    protected final static String CONFIG_FILE_NAME = "myconfig.properties";

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
}
