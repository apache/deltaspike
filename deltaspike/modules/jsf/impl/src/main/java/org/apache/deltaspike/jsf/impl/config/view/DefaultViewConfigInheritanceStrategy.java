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
import org.apache.deltaspike.core.api.config.view.metadata.Aggregated;
import org.apache.deltaspike.core.api.config.view.metadata.ViewMetaData;
import org.apache.deltaspike.core.spi.config.view.ViewConfigInheritanceStrategy;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.impl.util.ViewConfigUtils;

import javax.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
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
                inheritedAnnotations.addAll(findViewMetaData(currentClass, viewConfigNode));
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

        //add meta-data inherited via stereotypes on the node itself
        inheritedAnnotations.addAll(findViewMetaData(viewConfigNode.getSource(), viewConfigNode));

        return inheritedAnnotations;
    }

    protected List<Annotation> findViewMetaData(Class currentClass, ViewConfigNode viewConfigNode)
    {
        //don't include meta-data from the node itself, because it would be stored as inherited meta-data
        if (currentClass.equals(viewConfigNode.getSource()))
        {
            return Collections.emptyList();
        }

        List<Annotation> result = new ArrayList<Annotation>();

        for (Annotation annotation : currentClass.getAnnotations())
        {
            Class<? extends Annotation> annotationClass = annotation.annotationType();

            if (annotationClass.getName().startsWith("java"))
            {
                continue;
            }

            addViewMetaData(annotation, result);
        }

        result = tryToReplaceWithMergedMetaDataFromAncestor(currentClass, viewConfigNode.getParent(), result);
        return result;
    }

    //only supported for meta-data which isn't aggregated
    protected List<Annotation> tryToReplaceWithMergedMetaDataFromAncestor(
        Class currentClass, ViewConfigNode parentViewConfigNode, List<Annotation> foundResult)
    {
        ViewConfigNode ancestorNode = findNodeWithClass(currentClass, parentViewConfigNode);
        if (ancestorNode == null)
        {
            return foundResult;
        }

        List<Annotation> result = new ArrayList<Annotation>(foundResult.size());

        //only replace the meta-data found for the node and don't add all meta-data from the ancestor-node
        for (Annotation annotation : foundResult)
        {
            Annotation finalMetaData = getFinalMetaDataFromNode(ancestorNode, annotation);
            result.add(finalMetaData);
        }

        return result;
    }

    //the meta-data returned by this method is merged and potentially customized by a ConfigPreProcessor
    private Annotation getFinalMetaDataFromNode(ViewConfigNode viewConfigNode, Annotation annotation)
    {
        Class<? extends Annotation> targetType = annotation.annotationType();

        //skip @View and @Folder, because they get created dynamically to support their optional usage
        //the dynamic generation depends on the level and if it is a synthetic information
        if (View.class.equals(targetType) || Folder.class.equals(targetType))
        {
            return annotation;
        }

        //skip aggregated meta-data, because it can't be replaced
        //(there is no info available about the instance which replaced the original one
        // which might be equivalent to the annotation passed to this method)
        ViewMetaData viewMetaData = annotation.annotationType().getAnnotation(ViewMetaData.class);
        if (viewMetaData == null)
        {
            return annotation;
        }
        Aggregated aggregated = viewMetaData.annotationType().getAnnotation(Aggregated.class);
        if (aggregated == null || aggregated.value())
        {
            return annotation;
        }

        for (Annotation nodeMetaData : viewConfigNode.getMetaData())
        {
            if (targetType.equals(nodeMetaData.annotationType()))
            {
                return nodeMetaData;
            }
        }
        return annotation;
    }

    private ViewConfigNode findNodeWithClass(Class nodeClass, ViewConfigNode viewConfigNode)
    {
        if (viewConfigNode == null || nodeClass == null)
        {
            return null;
        }

        if (nodeClass.equals(viewConfigNode.getSource()))
        {
            return viewConfigNode;
        }
        return findNodeWithClass(nodeClass, viewConfigNode.getParent());
    }

    protected void addViewMetaData(Annotation currentAnnotation, List<Annotation> metaDataList)
    {
        Class<? extends Annotation> annotationClass = currentAnnotation.annotationType();

        if (annotationClass.isAnnotationPresent(ViewMetaData.class))
        {
            metaDataList.add(currentAnnotation);
        }

        if (annotationClass.isAnnotationPresent(Stereotype.class))
        {
            for (Annotation inheritedViaStereotype : annotationClass.getAnnotations())
            {
                if (inheritedViaStereotype.annotationType().isAnnotationPresent(ViewMetaData.class))
                {
                    metaDataList.add(inheritedViaStereotype);
                }
            }
        }
    }

    protected void addInterfaces(Set<Class> processedTypes, Stack<Class> classesToAnalyze, Class nextClass)
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
