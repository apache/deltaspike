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
package org.apache.deltaspike.test.core.api.config.injectable.numberconfig;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class NumberConfiguredBean
{
    @Inject
    @NumberConfig(name = "propertyFloat")
    private Float propertyFromConfig;

    @Inject
    @NumberConfig(name = "propertyNonexisting")
    private Float propertyNonexisting;

    @Inject
    @NumberConfig(name = "propertyNonexisting", defaultValue = "42.42")
    private Float propertyNonexistingDefaulted;


    protected NumberConfiguredBean()
    {
    }

    public Float getPropertyFromConfig()
    {
        return propertyFromConfig;
    }

    public Float getPropertyNonexisting()
    {
        return propertyNonexisting;
    }

    public Float getPropertyNonexistingDefaulted()
    {
        return propertyNonexistingDefaulted;
    }
}
