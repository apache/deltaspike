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

package org.apache.deltaspike.core.util.activation;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.base.CoreBaseConfig;
import org.apache.deltaspike.core.spi.activation.ClassDeactivator;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ServiceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class BaseClassDeactivationController
{
    private static final Logger LOG = Logger.getLogger(BaseClassDeactivationController.class.getName());

    /**
     * This Map holds the ClassLoader as first level to make it possible to have different configurations per
     * WebApplication in an EAR or other Multi-ClassLoader scenario.
     *
     * The Map then contains a List of {@link ClassDeactivator}s in order of their configured ordinal.
     */
    private static Map<ClassLoader, List<ClassDeactivator>> classDeactivatorMap =
        new ConcurrentHashMap<ClassLoader, List<ClassDeactivator>>();

    /**
     * Evaluates if the given {@link Deactivatable} is active.
     *
     * @param targetClass {@link Deactivatable} under test.
     * @return <code>true</code> if it is active, <code>false</code> otherwise
     */
    public boolean isActivated(Class<? extends Deactivatable> targetClass)
    {
        return calculateDeactivationStatusFor(targetClass);
    }

    /**
     * Determines if the given class is activated or not.  ClassDeactivators are cached at a class loader level.
     *
     * @param targetClass the Deactivatable class to search on
     * @return true if this class is activated, false if its not activated
     */
    protected static synchronized boolean calculateDeactivationStatusFor(Class<? extends Deactivatable> targetClass)
    {
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

        return isActivated;
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
        if (!ServiceUtils.loadServiceImplementations(ClassDeactivator.class).isEmpty())
        {
            CoreBaseConfig.Validation.ViolationMode violationMode = CoreBaseConfig.Validation.VIOLATION_MODE;

            String message = "It isn't supported to configure " + ClassDeactivator.class.getName() +
                    " via the std. service-loader config. " +
                    "Please remove all META-INF/services/" + ClassDeactivator.class.getName() + " files. " +
                    "Please configure it via the DeltaSpike-Config (e.g. META-INF/apache-deltaspike.properties).";

            if (violationMode == CoreBaseConfig.Validation.ViolationMode.FAIL)
            {
                throw new IllegalStateException(message);
            }
            else if (violationMode == CoreBaseConfig.Validation.ViolationMode.WARN)
            {
                LOG.warning(message);
            }
        }

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
