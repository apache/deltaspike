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

    /**
     * Cache for the result. It won't contain many classes but it might be accessed frequently.
     * Valid entries are only true or false. If an entry isn't available or null, it gets calculated.
     */
    private static Map<Class<? extends Deactivatable>, Boolean> activationStatusCache
        = new ConcurrentHashMap<Class<? extends Deactivatable>, Boolean>();
    
    private ClassDeactivation()
    {
        // private ct to prevent utility class from instantiation.
    }

    /**
     * Evaluates if the given {@link Deactivatable} is active.
     *
     * @param targetClass {@link Deactivatable} under test.
     * @return <code>true</code> if it is active, <code>false</code> otherwise
     */
    public static boolean isActivated(Class<? extends Deactivatable> targetClass)
    {
        Boolean activatedClassCacheEntry = activationStatusCache.get(targetClass);

        if (activatedClassCacheEntry == null)
        {
            initDeactivatableCacheFor(targetClass);
            activatedClassCacheEntry = activationStatusCache.get(targetClass);
        }
        return activatedClassCacheEntry;
    }

    private static synchronized void initDeactivatableCacheFor(Class<? extends Deactivatable> targetClass)
    {
        Boolean activatedClassCacheEntry = activationStatusCache.get(targetClass);

        if (activatedClassCacheEntry != null) //double-check
        {
            return;
        }

        List<ClassDeactivator> classDeactivators = getClassDeactivators();

        Boolean isActivated = Boolean.TRUE;
        Class<? extends ClassDeactivator> deactivatedBy = null;

        LOG.fine("start evaluation if " + targetClass.getName() + " is de-/activated");

        // we get the classActivators ordered by it's ordinal
        // thus the last one which returns != null 'wins' ;)
        for (ClassDeactivator classDeactivator : classDeactivators)
        {
            Boolean isLocallyActivated = classDeactivator.isActivated(targetClass);

            if (isLocallyActivated != null)
            {
                isActivated = isLocallyActivated;

                /*
                * Check and log the details across class-deactivators
                */
                if (!isActivated)
                {
                    deactivatedBy = classDeactivator.getClass();
                    LOG.fine("Deactivating class " + targetClass);
                }
                else if (deactivatedBy != null)
                {
                    LOG.fine("Reactivation of: " + targetClass.getName() + " by " +
                            classDeactivator.getClass().getName() +
                            " - original deactivated by: " + deactivatedBy.getName() + ".\n" +
                            "If that isn't the intended behaviour, you have to use a higher ordinal for " +
                            deactivatedBy.getName());
                }
            }
        }

        cacheResult(targetClass, isActivated);
    }

    private static void cacheResult(Class<? extends Deactivatable> targetClass, Boolean activated)
    {
        activationStatusCache.put(targetClass, activated);
        LOG.info("class: " + targetClass.getName() + " activated=" + activated);
    }

    /**
     * @return the List of configured @{link ClassDeactivator}s for the current context ClassLoader.
     */
    private static List<ClassDeactivator> getClassDeactivators()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);
        List<ClassDeactivator> classDeactivators = classDeactivatorMap.get(classLoader);

        if (classDeactivators == null)
        {
            return initConfiguredClassDeactivators(classLoader);
        }

        return classDeactivators;
    }

    //synchronized isn't needed - #initDeactivatableCacheFor is already synchronized
    private static List<ClassDeactivator> initConfiguredClassDeactivators(ClassLoader classLoader)
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
                throw new IllegalStateException(e);
            }
        }

        classDeactivatorMap.put(classLoader, classDeactivators);
        return classDeactivators;
    }
}
