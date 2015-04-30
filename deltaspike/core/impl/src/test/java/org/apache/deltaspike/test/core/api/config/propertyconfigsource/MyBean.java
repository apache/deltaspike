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

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.config.ConfigProperty;

@Stateless
public class MyBean
{

    @Inject
    @ConfigProperty(name = "configproperty.test.string")
    private String stringConfig;

    @Inject
    @ConfigProperty(name = "INVALIDKEY", defaultValue = "DEFAULT")
    private String stringConfigWithDefault;

    @Inject
    @ConfigProperty(name = "configproperty.test.string", projectStageAware = false)
    private String stringConfigWithoutProjectStage;

    @Inject
    @ConfigProperty(name = "configproperty.test.string", projectStageAware = true,
            parameterizedBy = "configproperty.test.param")
    private String stringConfigParameterized;

    @Inject
    @ConfigProperty(name = "configproperty.test.boolean")
    private Boolean booleanConfig;

    @Inject
    @ConfigProperty(name = "configproperty.test.class")
    private Class classConfig;

    @Inject
    @ConfigProperty(name = "configproperty.test.int")
    private Integer intConfig;

    @Inject
    @ConfigProperty(name = "configproperty.test.long")
    private Long longConfig;

    @Inject
    @ConfigProperty(name = "configproperty.test.float")
    private Float floatConfig;

    @Inject
    @ConfigProperty(name = "configproperty.test.double")
    private Double doubleConfig;

    public String getStringConfig()
    {
        return stringConfig;
    }

    public String getStringConfigWithDefault()
    {
        return stringConfigWithDefault;
    }

    public String getStringConfigWithoutProjectStage()
    {
        return stringConfigWithoutProjectStage;
    }

    public String getStringConfigParameterized()
    {
        return stringConfigParameterized;
    }

    public boolean getBooleanConfig()
    {
        return booleanConfig;
    }

    public Class getClassConfig()
    {
        return classConfig;
    }

    public int getIntConfig()
    {
        return intConfig;
    }

    public long getLongConfig()
    {
        return longConfig;
    }

    public float getFloatConfig()
    {
        return floatConfig;
    }

    public double getDoubleConfig()
    {
        return doubleConfig;
    }
}
