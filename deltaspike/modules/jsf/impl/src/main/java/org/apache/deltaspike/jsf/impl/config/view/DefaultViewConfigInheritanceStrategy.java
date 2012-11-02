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
package org.apache.deltaspike.jsf.impl.config.view;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.spi.config.view.ViewConfigInheritanceStrategy;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.impl.util.ViewConfigUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

//TODO remove parts which aren't needed any longer
public class DefaultViewConfigInheritanceStrategy implements ViewConfigInheritanceStrategy
{
    @Override
    public List<Annotation> resolveInheritedMetaData(ViewConfigNode viewConfigNode)
    {
        List<Annotation> inheritedAnnotations = new ArrayList<Annotation>();

        Set<Class> processedTypes = new HashSet<Class>();
        processedTypes.add(ViewConfig.class); //filter the base interface in any case

        Stack<Class> classesToAnalyze = new Stack<Class>();
        addInterfaces(processedTypes, classesToAnalyze, viewConfigNode.getSource()); //don't add the page-class itself

        while (!classesToAnalyze.empty())
        {
            Class currentClass = classesToAnalyze.pop();

            if (processedTypes.contains(currentClass))
            {
                continue;
            }
            processedTypes.add(currentClass);

            addInterfaces(processedTypes, classesToAnalyze, currentClass);

            //don't add the annotations of the final view-config class itself (we just need the inherited annotations)
            if (ViewConfigUtils.isFolderConfig(currentClass))
            {
                inheritedAnnotations.addAll(Arrays.asList(currentClass.getAnnotations()));
            }

            Class nextClass = currentClass.getSuperclass();
            if (nextClass != null && !Object.class.equals(nextClass))
            {
                if (!processedTypes.contains(nextClass))
                {
                    classesToAnalyze.push(nextClass);
                }
            }
        }
        return inheritedAnnotations;
    }

    private void addInterfaces(Set<Class> processedTypes, Stack<Class> classesToAnalyze, Class nextClass)
    {
        for (Class<?> interfaceToAdd : nextClass.getInterfaces())
        {
            addInterfaces(processedTypes, classesToAnalyze, interfaceToAdd);

            if (!processedTypes.contains(interfaceToAdd))
            {
                classesToAnalyze.push(interfaceToAdd);
            }
        }
    }
}
