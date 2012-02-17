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
package org.apache.deltaspike.containerctrl.api;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * <p>This class provides access to the ContainerControl.</p>
 * <p>It uses the {@code java.util.ServiceLoader} mechanism  to 
 * automatically pickup the container providers from the classpath.</p>
 */
public final class ContainerControlLoader
{
    private ContainerControlLoader()
    {
        // private ct to prevent instantiation
    }

    
    public static ContainerControl getCdiContainer()
    {
        ContainerControl testContainer = null;

        //doesn't support the implementation loader (there is no dependency to owb-impl
        ServiceLoader<ContainerControl> cdiContainerLoader = ServiceLoader.load(ContainerControl.class);
        Iterator<ContainerControl> cdiIt = cdiContainerLoader.iterator();
        if (cdiIt.hasNext())
        {
            testContainer = cdiIt.next();
        }
        else 
        {
            throw new RuntimeException("Could not find a ContainerControl available in the classpath!");
        }
        
        if (cdiIt.hasNext())
        {
            throw new RuntimeException("Too many ContainerControl found in the classpath!");
        }
        
        return testContainer;
    }
}
