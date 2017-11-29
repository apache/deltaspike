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
package org.apache.deltaspike.test.core.api.config.injectable;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.config.Configuration;

import java.net.URL;
import java.util.List;
import java.util.Set;

@Configuration
public interface ConfigBean
{
    @ConfigProperty(name = "configProperty1")
    int intProperty1();

    @ConfigProperty(name = "configProperty1", defaultValue = "myDefaultValue")
    String stringProperty3Filled();

    @ConfigProperty(name = "nonexistingProperty", defaultValue = "myDefaultValue")
    String stringProperty3Defaulted();

    @ConfigProperty(name = "nonexistingProperty", defaultValue = "42")
    Integer intProperty4Defaulted();

    @ConfigProperty(name = "configProperty1")
    Boolean booleanPropertyNull();

    @ConfigProperty(name = "configProperty1", defaultValue = "false")
    boolean booleanPropertyFalse();

    @ConfigProperty(name = "configPropertyTrue1")
    Boolean booleanPropertyTrue1();

    @ConfigProperty(name = "configPropertyTrue2")
    Boolean booleanPropertyTrue2();

    @ConfigProperty(name = "configPropertyTrue3")
    Boolean booleanPropertyTrue3();

    @ConfigProperty(name = "configPropertyTrue4")
    Boolean booleanPropertyTrue4();

    @ConfigProperty(name = "configPropertyTrue5")
    Boolean booleanPropertyTrue5();

    @ConfigProperty(name = "configPropertyTrue6")
    Boolean booleanPropertyTrue6();

    @ConfigProperty(name = "configPropertyTrue7")
    Boolean booleanPropertyTrue7();

    @ConfigProperty(name = "configPropertyTrue8")
    Boolean booleanPropertyTrue8();

    @ConfigProperty(name = "testDbConfig")
    String dbConfig();

    @ConfigProperty(name = "defaultList", defaultValue = "http://localhost,http://127.0.0.1")
    List<String> defaultListHandling();

    @ConfigProperty(name = "defaultSet", defaultValue = "1,2")
    Set<Integer> defaultSetHandling();

    @ConfigProperty(name = "urlList", converter = SettingsBean.UrlList.class, defaultValue = "http://localhost,http://127.0.0.1")
    List<URL> urlList();

    @ConfigProperty(name = "urlListFromProperties", converter = SettingsBean.UrlList.class)
    List<URL> urlListFromProperties();

    @ConfigProperty(name = "custom-source.test")
    String customSourceValue();
}
