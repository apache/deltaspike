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
import org.apache.deltaspike.core.api.config.ConfigResolver;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SettingsBean
{
    final static String PROPERTY_NAME = "configProperty2";

    @Inject
    @ConfigProperty(name = "configProperty1")
    private Integer intProperty1;

    private Long property2;

    @Inject
    @ConfigProperty(name = "configProperty1", defaultValue = "myDefaultValue")
    private String stringProperty3Filled;

    @Inject
    @ConfigProperty(name = "nonexistingProperty", defaultValue = "myDefaultValue")
    private String stringProperty3Defaulted;

    @Inject
    @ConfigProperty(name = "nonexistingProperty", defaultValue = "42")
    private Integer intProperty4Defaulted;

    private Long inverseProperty2;

    @Inject
    @ConfigProperty(name = "configProperty1")
    private Boolean booleanPropertyNull;

    @Inject
    @ConfigProperty(name = "configProperty1", defaultValue = "false")
    private Boolean booleanPropertyFalse;

    @Inject
    @ConfigProperty(name = "configPropertyTrue1")
    private Boolean booleanPropertyTrue1;

    @Inject
    @ConfigProperty(name = "configPropertyTrue2")
    private Boolean booleanPropertyTrue2;

    @Inject
    @ConfigProperty(name = "configPropertyTrue3")
    private Boolean booleanPropertyTrue3;

    @Inject
    @ConfigProperty(name = "configPropertyTrue4")
    private Boolean booleanPropertyTrue4;

    @Inject
    @ConfigProperty(name = "configPropertyTrue5")
    private Boolean booleanPropertyTrue5;

    @Inject
    @ConfigProperty(name = "configPropertyTrue6")
    private Boolean booleanPropertyTrue6;

    @Inject
    @ConfigProperty(name = "configPropertyTrue7")
    private Boolean booleanPropertyTrue7;

    @Inject
    @ConfigProperty(name = "configPropertyTrue8")
    private Boolean booleanPropertyTrue8;

    @Inject
    @ConfigProperty(name = "testDbConfig")
    private String dbConfig;

    @Inject
    @ConfigProperty(name = "urlList", converter = UrlList.class, defaultValue = "http://localhost,http://127.0.0.1")
    private List<URL> urlList;

    @Inject
    @ConfigProperty(name = "urlListFromProperties", converter = UrlList.class)
    private List<URL> urlListFromProperties;

    @Inject
    @ConfigProperty(name = "custom-source.test")
    private String customSourceValue;

    @Inject
    @ConfigProperty(name = "myapp.login.url")
    private String projectStageAwareVariableValue;

    protected SettingsBean()
    {
    }

    @Inject
    public SettingsBean(@ConfigProperty(name= PROPERTY_NAME) Long property2)
    {
        this.property2 = property2;
    }

    @Inject
    protected void init(@CustomConfigAnnotationWithMetaData(inverseConvert = true) Long inverseProperty)
    {
        inverseProperty2 = inverseProperty;
    }

    int getProperty1()
    {
        return intProperty1;
    }

    long getProperty2()
    {
        return property2;
    }

    long getInverseProperty2()
    {
        return inverseProperty2;
    }

    public String getProperty3Defaulted()
    {
        return stringProperty3Defaulted;
    }

    public String getProperty3Filled()
    {
        return stringProperty3Filled;
    }

    public int getProperty4Defaulted()
    {
        return intProperty4Defaulted;
    }


    public Boolean getBooleanPropertyNull()
    {
        return booleanPropertyNull;
    }

    public boolean getBooleanPropertyFalse()
    {
        return booleanPropertyFalse;
    }

    public Boolean getBooleanPropertyTrue1()
    {
        return booleanPropertyTrue1;
    }

    public Boolean getBooleanPropertyTrue2()
    {
        return booleanPropertyTrue2;
    }

    public Boolean getBooleanPropertyTrue3()
    {
        return booleanPropertyTrue3;
    }

    public Boolean getBooleanPropertyTrue4()
    {
        return booleanPropertyTrue4;
    }

    public Boolean getBooleanPropertyTrue5()
    {
        return booleanPropertyTrue5;
    }

    public Boolean getBooleanPropertyTrue6()
    {
        return booleanPropertyTrue6;
    }

    public Boolean getBooleanPropertyTrue7()
    {
        return booleanPropertyTrue7;
    }

    public Boolean getBooleanPropertyTrue8()
    {
        return booleanPropertyTrue8;
    }

    public String getDbConfig()
    {
        return dbConfig;
    }

    public List<URL> getUrlList() {
        return urlList;
    }

    public List<URL> getUrlListFromProperties() {
        return urlListFromProperties;
    }

    public String getCustomSourceValue() {
        return customSourceValue;
    }

    public String getProjectStageAwareVariableValue()
    {
        return projectStageAwareVariableValue;
    }

    public static class UrlList implements ConfigResolver.Converter<List<URL>>
    {
        @Override
        public List<URL> convert(final String value)
        {
            final List<URL> urls = new ArrayList<URL>();
            if (value != null)
            {
                for (final String segment : value.split(","))
                {
                    try
                    {
                        urls.add(new URL(segment));
                    }
                    catch (final MalformedURLException e)
                    {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
            return urls;
        }
    }
}
