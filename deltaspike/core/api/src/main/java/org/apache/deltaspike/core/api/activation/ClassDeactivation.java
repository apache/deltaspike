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
package org.apache.deltaspike.core.api.activation;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.util.ClassUtils;

import javax.enterprise.inject.Typed;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Helper methods for {@link ClassDeactivator}
 */
@Typed()
public final class ClassDeactivation
{
    private static final Logger LOG = Logger.getLogger(ClassDeactivation.class.getName());

    /**
     * This Map holds the ClassLoader as first level to make it possible to have different configurations per 
     * WebApplication in an EAR or other Multi-ClassLoader scenario.
     * 
     * The Map then contains a List of {@link ClassDeactivator}s in order of their configured ordinal.
     */
    private static Map<ClassLoader, List<ClassDeactivator>> classDeactivatorMap
        = new ConcurrentHashMap<ClassLoader, List<ClassDeactivator>>();

    private ClassDeactivation()
    {
        // private ct to prevent utility class from instantiation.
    }

    /**
     * Evaluates if the given {@link Deactivatable} is active.
     *
     * @param deactivatableClazz {@link Deactivatable} under test.
     * @return <code>true</code> if it is active, <code>false</code> otherwise
     */
    public static synchronized boolean isActivated(Class<? extends Deactivatable> deactivatableClazz)
    {
        List<ClassDeactivator> classDeactivators = getClassDeactivators();

        Boolean isActivated = Boolean.TRUE; // by default a class is always activated.

        for (ClassDeactivator classDeactivator : classDeactivators)
        {
            Boolean isLocallyActivated = classDeactivator.isActivated(deactivatableClazz);
            if (isLocallyActivated != null)
            {
                isActivated = isLocallyActivated;
            }
        }
        
        if (!isActivated) 
        {
            LOG.info("Deactivating class " + deactivatableClazz);
        }

        return isActivated;
    }


    /**
     * @return the List of configured @{link ClassDeactivator}s for the current context ClassLoader.
     */
    private static List<ClassDeactivator> getClassDeactivators()
    {
        List<ClassDeactivator> classDeactivators = classDeactivatorMap.get(ClassUtils.getClassLoader(null));
        if (classDeactivators == null)
        {
            classDeactivators = getConfiguredClassDeactivator();
        }

        return classDeactivators;
    }

    private static List<ClassDeactivator> getConfiguredClassDeactivator()
    {
        List<String> classDeactivatorClassNames = ConfigResolver.getAllPropertyValues(ClassDeactivator.class.getName());

        List<ClassDeactivator> classDeactivators = new ArrayList<ClassDeactivator>();

        for (String classDeactivatorClassName : classDeactivatorClassNames)
        {
            LOG.fine("processing ClassDeactivator: " + classDeactivatorClassName);

            try
            {
                ClassDeactivator currentClassDeactivator =
                        (ClassDeactivator) ClassUtils.instantiateClassForName(classDeactivatorClassName);
                classDeactivators.add(currentClassDeactivator);
            }
            catch (Exception e)
            {
                LOG.warning(classDeactivatorClassName + " can't be instantiated");
                throw new RuntimeException(e);
            }
        }

        return classDeactivators;
    }
}
