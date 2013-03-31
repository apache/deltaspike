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
package org.apache.deltaspike.example.config;

import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SettingsBean
{
    @Inject
    @ConfigProperty(name = "property1")
    private Integer intProperty1;

    @Inject
    @Location
    private LocationId locationId;

    private Long property2;

    private Long inverseProperty;

    protected SettingsBean()
    {
    }

    @Inject
    public SettingsBean(@Property2 Long property2)
    {
        this.property2 = property2;
    }

    @Inject
    protected void init(@Property2WithInverseSupport(inverseConvert = true) Long inverseProperty)
    {
        this.inverseProperty = inverseProperty;
    }

    public Integer getIntProperty1()
    {
        return intProperty1;
    }

    public Long getProperty2()
    {
        return property2;
    }

    public Long getInverseProperty()
    {
        return inverseProperty;
    }

    public LocationId getLocationId()
    {
        return locationId;
    }
}
