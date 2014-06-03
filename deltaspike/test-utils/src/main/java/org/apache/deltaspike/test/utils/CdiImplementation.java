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
package org.apache.deltaspike.test.utils;

public enum CdiImplementation
{
    OWB11 ("org.apache.webbeans.container.BeanManagerImpl", "[1.1,1.2)"),
    OWB12 ("org.apache.webbeans.container.BeanManagerImpl", "[1.2,1.3)"),

    WELD11("org.jboss.weld.manager.BeanManagerImpl", "[1.1,1.2)"),
    WELD12("org.jboss.weld.manager.BeanManagerImpl", "[1.2,1.3)"),
    WELD20("org.jboss.weld.manager.BeanManagerImpl", "[2.0,2.1)");

    private final String implementationClassName;
    private final String versionRange;

    CdiImplementation(String implementationClassName, String versionRange)
    {
        this.implementationClassName = implementationClassName;
        this.versionRange = versionRange;
    }

    public String getImplementationClassName()
    {
        return implementationClassName;
    }

    public String getVersionRange()
    {
        return versionRange;
    }
}
