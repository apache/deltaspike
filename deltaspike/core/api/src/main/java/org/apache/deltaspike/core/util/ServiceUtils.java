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
package org.apache.deltaspike.core.util;

import javax.enterprise.inject.Typed;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Allows handling the lookup (with fallbacks) in a central place.
 * See DELTASPIKE-97
 */
@Typed()
public abstract class ServiceUtils
{
    private ServiceUtils()
    {
        // prevent instantiation
    }

    public static <T> List<T> loadServiceImplementations(Class<T> serviceType)
    {
        List<T> result = new ArrayList<T>();

        Iterator<T> servicesIterator = ServiceLoader.load(serviceType).iterator();

        if (!servicesIterator.hasNext())
        {
            //X TODO check if we have to do it in any case with different packaging constellations
            //X and merge the result in any case
            ClassLoader fallbackClassLoader = ServiceUtils.class.getClassLoader();
            servicesIterator = ServiceLoader.load(serviceType, fallbackClassLoader).iterator();
        }

        while (servicesIterator.hasNext())
        {
            result.add(servicesIterator.next());
        }
        return result;
    }
}
