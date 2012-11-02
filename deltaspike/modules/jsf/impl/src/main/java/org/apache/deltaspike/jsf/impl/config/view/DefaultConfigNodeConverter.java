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

import org.apache.deltaspike.core.api.config.view.metadata.annotation.Aggregated;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.annotation.ViewMetaData;
import org.apache.deltaspike.core.api.config.view.metadata.ConfigDescriptor;
import org.apache.deltaspike.core.spi.config.view.ConfigNodeConverter;
import org.apache.deltaspike.core.spi.config.view.ConfigPreProcessor;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.Page;
import org.apache.deltaspike.jsf.impl.util.ViewConfigUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultConfigNodeConverter implements ConfigNodeConverter
{
    @Override
    public ConfigDescriptor convert(ViewConfigNode node)
    {
        List<Annotation> mergedMetaData = mergeMetaData(node.getMetaData(), node.getInheritedMetaData());
        //e.g. replace default placeholders needed for the merge with real default values
        mergedMetaData = preProcessMetaData(mergedMetaData, node);

        Class sourceClass = node.getSource();

        if (ViewConfigUtils.isFolderConfig(sourceClass))
        {
            Folder folderAnnotation = findMetaDataByType(mergedMetaData, Folder.class);
            return new DefaultFolderConfigDescriptor(folderAnnotation.name(), node.getSource(),
                    mergedMetaData, node.getCallbackDescriptors());
        }
        else if (ViewConfig.class.isAssignableFrom(sourceClass))
        {
            Page pageAnnotation = findMetaDataByType(mergedMetaData, Page.class);
            String viewId = pageAnnotation.basePath() + pageAnnotation.name() + "." + pageAnnotation.extension();
            return new DefaultViewPathConfigDescriptor(viewId, node.getSource(),
                    mergedMetaData, node.getCallbackDescriptors());
        }
        else
        {
            throw new IllegalStateException(node.getSource() + " isn't a valid view-config");
        }
    }

    private <T> T findMetaDataByType(List<Annotation> metaData, Class<T> target)
    {
        for (Annotation annotation : metaData)
        {
            if (target.equals(annotation.annotationType()))
            {
                return (T) annotation;
            }
        }

        return null;
    }

    private List<Annotation> mergeMetaData(Set<Annotation> metaData, List<Annotation> inheritedMetaData)
    {
        //TODO add qualifier support
        List<Annotation> nodeViewMetaData = new ArrayList<Annotation>();

        for (Annotation annotation : metaData)
        {
            if (annotation.annotationType().isAnnotationPresent(ViewMetaData.class))
            {
                nodeViewMetaData.add(annotation);
            }
        }

        if (inheritedMetaData == null || inheritedMetaData.isEmpty())
        {
            return nodeViewMetaData;
        }

        List<Annotation> mergedResult = new ArrayList<Annotation>();

        for (Annotation currentAnnotation : inheritedMetaData)
        {
            ViewMetaData viewMetaData = currentAnnotation.annotationType().getAnnotation(ViewMetaData.class);

            if (viewMetaData == null)
            {
                continue;
            }

            Aggregated aggregated = currentAnnotation.annotationType().getAnnotation(Aggregated.class);

            if (aggregated == null)
            {
                aggregated = viewMetaData.annotationType().getAnnotation(Aggregated.class);
            }

            if (aggregated.value()) //aggregation for the whole annotation is allowed
            {
                mergedResult.add(currentAnnotation);
            }
            else
            {
                Annotation existingMetaData = findInResult(nodeViewMetaData, currentAnnotation);
                if (existingMetaData == null)
                {
                    Annotation mergedMetaData = findInResult(mergedResult, currentAnnotation);

                    if (mergedMetaData == null)
                    {
                        mergedResult.add(currentAnnotation);
                    }
                    else
                    {
                        Annotation mergedAnnotation = mergeAnnotationInstance(mergedMetaData, currentAnnotation);
                        mergedResult.add(mergedAnnotation);
                    }
                }
                else
                {
                    Annotation mergedAnnotation = mergeAnnotationInstance(existingMetaData, currentAnnotation);
                    mergedResult.add(mergedAnnotation);
                }
            }
        }

        //add all annotations at the beginning which weren't used for the merge
        mergedResult.addAll(0, nodeViewMetaData);
        return mergedResult;
    }

    private Annotation mergeAnnotationInstance(Annotation existingMetaData, Annotation inheritedMetaData)
    {
        Map<String, Object> values = new HashMap<String, Object>();

        for (Method annotationMethod : existingMetaData.annotationType().getDeclaredMethods())
        {
            //TODO discuss @ExcludeMethod
            annotationMethod.setAccessible(true); //TODO

            Annotation defaultAnnotation = AnnotationInstanceProvider.of(existingMetaData.annotationType());
            try
            {
                Object defaultValue = annotationMethod.invoke(defaultAnnotation);
                Object existingValue = annotationMethod.invoke(existingMetaData);

                if (existingValue == null /*possible with literal instances*/ ||
                        existingValue.equals(defaultValue))
                {
                    Object inheritedValue = annotationMethod.invoke(inheritedMetaData);

                    if (inheritedValue == null /*possible with literal instances*/ ||
                            inheritedValue.equals(defaultValue))
                    {
                        values.put(annotationMethod.getName(), defaultValue);
                    }
                    else
                    {
                        values.put(annotationMethod.getName(), inheritedValue);
                    }
                }
                else
                {
                    values.put(annotationMethod.getName(), existingValue);
                }
            }
            catch (Exception e)
            {
                ExceptionUtils.throwAsRuntimeException(e);
            }
        }
        //TODO add aggregation in case of arrays
        return AnnotationInstanceProvider.of(existingMetaData.annotationType(), values);
    }

    private List<Annotation> preProcessMetaData(List<Annotation> mergedMetaData, ViewConfigNode node)
    {
        List<Annotation> result = new ArrayList<Annotation>(mergedMetaData.size());

        for (Annotation annotation : mergedMetaData)
        {
            ViewMetaData viewMetaData = annotation.annotationType().getAnnotation(ViewMetaData.class);
            Class<? extends ConfigPreProcessor> preProcessorClass = viewMetaData.preProcessor();
            if (!ConfigPreProcessor.class.equals(preProcessorClass))
            {
                ConfigPreProcessor preProcessor = ClassUtils.tryToInstantiateClass(preProcessorClass);
                result.add(preProcessor.beforeAddToConfig(annotation, node));
            }
            else
            {
                result.add(annotation);
            }
        }

        return result;
    }

    private Annotation findInResult(List<Annotation> annotationList, Annotation annotationToFind)
    {
        for (Annotation annotation : annotationList)
        {
            if (annotationToFind.annotationType().equals(annotation.annotationType()))
            {
                annotationList.remove(annotation);
                return annotation;
            }
        }
        return null;
    }
}
