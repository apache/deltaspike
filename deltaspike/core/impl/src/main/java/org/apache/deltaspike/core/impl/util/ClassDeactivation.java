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
package org.apache.deltaspike.core.impl.util;

import org.apache.deltaspike.core.api.activation.ClassDeactivator;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.util.ClassUtils;

import javax.enterprise.inject.Typed;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Helper methods for {@link ClassDeactivator}
 */
@Typed()
public final class ClassDeactivation
{
    private static final Logger LOG = Logger.getLogger(ClassDeactivation.class.getName());

    private ClassDeactivation()
    {
    }

    /**
     * Evaluates if the given class is active
     *
     * @param targetClass current class
     * @return true if it is active, false otherwise
     */
    public static boolean isClassActivated(Class targetClass)
    {
        ClassDeactivator classDeactivator = ClassDeactivatorStorage.getClassDeactivator();

        if (classDeactivator == null)
        {
            classDeactivator = resolveAndCacheClassDeactivator();
        }

        boolean classDeactivated = classDeactivator.getDeactivatedClasses().contains(targetClass);

        return !classDeactivated;
    }

    /**
     * Allows to provide a custom {@link ClassDeactivator}
     *
     * @param classDeactivator class-deactivator which should be used
     */
    public static void setClassDeactivator(ClassDeactivator classDeactivator)
    {
        ClassDeactivatorStorage.setClassDeactivator(classDeactivator);
    }

    private static ClassDeactivator resolveAndCacheClassDeactivator()
    {
        ClassDeactivator classDeactivator = getConfiguredClassDeactivator();

        // display deactivated classes here once
        // NOTE that isClassActivated() will be called many times for the same class
        for (Class<?> deactivatedClass : classDeactivator.getDeactivatedClasses())
        {
            LOG.info("deactivate: " + deactivatedClass);
        }

        ClassDeactivatorStorage.setClassDeactivator(classDeactivator);
        return classDeactivator;
    }

    private static ClassDeactivator getConfiguredClassDeactivator()
    {
        List<String> classDeactivatorClassNames = ConfigResolver.getAllPropertyValues(ClassDeactivator.class.getName());
        Set<Class> deactivatedClasses = new HashSet<Class>();

        ClassDeactivator currentClassDeactivator;
        for(String classDeactivatorClassName : classDeactivatorClassNames)
        {
            LOG.info(classDeactivatorClassName + " gets processed");

            currentClassDeactivator =
                    ClassUtils.tryToInstantiateClassForName(classDeactivatorClassName, ClassDeactivator.class);

            if(currentClassDeactivator != null)
            {
                deactivatedClasses.addAll(currentClassDeactivator.getDeactivatedClasses());
            }
            else
            {
                LOG.warning(classDeactivatorClassName + " can't be instantiated");
            }
        }

        return new DefaultClassDeactivator(deactivatedClasses);
    }
}
