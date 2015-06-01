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
package org.apache.deltaspike.proxy.api;

import java.util.List;

import org.apache.deltaspike.core.util.ServiceUtils;
import org.apache.deltaspike.proxy.spi.ProxyClassGenerator;

/**
 * Looks up a {@link ProxyClassGenerator} via Java SE ServiceLoader or via Declarative Services
 * injection when running under OSGi.
 */
public class ProxyClassGeneratorLookup
{

    private static ProxyClassGenerator generator;

    /**
     * Looks up a unique service implementation.
     * 
     * @return ProxyClassGenerator service
     */
    public static ProxyClassGenerator lookupService()
    {
        if (generator == null)
        {
            List<ProxyClassGenerator> proxyClassGeneratorList = ServiceUtils
                .loadServiceImplementations(ProxyClassGenerator.class);

            if (proxyClassGeneratorList.size() != 1)
            {
                throw new IllegalStateException(proxyClassGeneratorList.size()
                    + " implementations of " + ProxyClassGenerator.class.getName()
                    + " found. Expected exactly one implementation.");
            }
            generator = proxyClassGeneratorList.get(0);
        }
        return generator;
    }

    /**
     * Setter invoked by OSGi Service Component Runtime
     * 
     * @param generator
     *            generator service
     */
    public void setGenerator(ProxyClassGenerator generator)
    {
        ProxyClassGeneratorLookup.generator = generator;
    }
}
