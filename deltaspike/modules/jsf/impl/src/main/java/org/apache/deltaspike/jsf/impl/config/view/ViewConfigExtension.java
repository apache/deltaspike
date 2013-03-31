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
import org.apache.deltaspike.core.api.config.view.metadata.InlineViewMetaData;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.spi.config.view.ConfigNodeConverter;
import org.apache.deltaspike.core.spi.config.view.InlineMetaDataTransformer;
import org.apache.deltaspike.core.spi.config.view.TargetViewConfigProvider;
import org.apache.deltaspike.core.spi.config.view.ViewConfigInheritanceStrategy;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.core.spi.config.view.ViewConfigRoot;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.jsf.impl.util.ViewConfigUtils;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewConfigExtension implements Extension, Deactivatable
{
    private boolean isActivated = true;

    private ViewConfigNode rootViewConfigNode;

    private ViewConfigResolver viewConfigResolver;
    private boolean transformed = false;

    {
        resetRootNode();
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void buildViewConfigMetaDataTree(@Observes ProcessAnnotatedType pat)
    {
        if (!isActivated)
        {
            return;
        }

        if (ViewConfig.class.isAssignableFrom(pat.getAnnotatedType().getJavaClass()))
        {
            addPageDefinition(pat.getAnnotatedType().getJavaClass(), pat.getAnnotatedType().getAnnotations());
            pat.veto();
        }
        else
        {
            addIndirectlyInheritedMetaData(
                pat.getAnnotatedType().getJavaClass(), pat.getAnnotatedType().getAnnotations());
        }
    }

    public void addIndirectlyInheritedMetaData(Class configClass)
    {
        addIndirectlyInheritedMetaData(
            configClass, new HashSet<Annotation>(Arrays.asList(configClass.getAnnotations())));
    }

    protected void addIndirectlyInheritedMetaData(Class configClass, Set<Annotation> annotations)
    {
        for (Annotation annotation : annotations)
        {
            InlineViewMetaData inlineViewMetaData = annotation.annotationType().getAnnotation(InlineViewMetaData.class);
            if (inlineViewMetaData != null)
            {
                Class<? extends TargetViewConfigProvider> targetViewConfigProviderClass =
                        inlineViewMetaData.targetViewConfigProvider();
                TargetViewConfigProvider targetViewConfigProvider =
                        ClassUtils.tryToInstantiateClass(targetViewConfigProviderClass);

                for (Class<? extends ViewConfig> viewConfigRef : targetViewConfigProvider.getTarget(annotation))
                {
                    ViewConfigNode viewConfigNode = findNode(viewConfigRef);

                    if (viewConfigNode == null)
                    {
                        addPageDefinition(viewConfigRef);
                        viewConfigNode = findNode(viewConfigRef);

                        if (viewConfigNode == null)
                        {
                            throw new IllegalStateException("No node created for: " + viewConfigRef);
                        }
                    }

                    Class<? extends InlineMetaDataTransformer> inlineNodeTransformerClass =
                            inlineViewMetaData.inlineMetaDataTransformer();

                    if (!InlineMetaDataTransformer.class.equals(inlineNodeTransformerClass))
                    {
                        InlineMetaDataTransformer inlineMetaDataTransformer =
                                ClassUtils.tryToInstantiateClass(inlineNodeTransformerClass);

                        viewConfigNode.getInheritedMetaData().add(
                                inlineMetaDataTransformer.convertToViewMetaData(annotation, configClass));
                    }
                    else //no custom transformer registered -> add the annotation itself
                    {
                        viewConfigNode.getInheritedMetaData().add(annotation);
                    }
                }
                break;
            }
        }
    }

    public void addPageDefinition(Class<? extends ViewConfig> viewConfigClass)
    {
        addPageDefinition(viewConfigClass, new HashSet<Annotation>(Arrays.asList(viewConfigClass.getAnnotations())));
    }

    protected void addPageDefinition(Class<? extends ViewConfig> viewConfigClass, Set<Annotation> viewConfigAnnotations)
    {
        for (Annotation annotation : viewConfigAnnotations)
        {
            if (annotation.annotationType().equals(ViewConfigRoot.class))
            {
                if (this.rootViewConfigNode.getSource() != null)
                {
                    throw new IllegalStateException("@" + ViewConfigRoot.class.getName() + " has been found at " +
                            viewConfigClass.getName() + " and " + this.rootViewConfigNode.getSource().getName());
                }
                this.rootViewConfigNode.getMetaData().add(annotation);
                this.rootViewConfigNode = new FolderConfigNode(this.rootViewConfigNode, viewConfigClass);
                break;
            }
        }

        if (ViewConfigUtils.isFolderConfig(viewConfigClass))
        {
            //TODO log it
            return;
        }

        List<Class> treePath = ViewConfigUtils.toNodeList(viewConfigClass);

        ViewConfigNode previousRootNode = null;
        for (Class currentNode : treePath)
        {
            //can only return a node if a folder was added already
            ViewConfigNode baseNode = findNode(currentNode);
            if (baseNode == null)
            {
                Set<Annotation> metaData = viewConfigAnnotations;

                if (!currentNode.equals(viewConfigClass)) //small tweak
                {
                    metaData = new HashSet<Annotation>(Arrays.asList(currentNode.getAnnotations()));
                }

                previousRootNode = addNode(previousRootNode, currentNode, metaData);
            }
            else
            {
                previousRootNode = baseNode;
            }
        }
    }

    private ViewConfigNode addNode(ViewConfigNode parentNode, Class idOfNewNode, Set<Annotation> viewConfigAnnotations)
    {
        if (parentNode == null)
        {
            parentNode = this.rootViewConfigNode;
        }

        ViewConfigNode viewConfigNode;

        if (ViewConfigUtils.isFolderConfig(idOfNewNode))
        {
            viewConfigNode = new FolderConfigNode(idOfNewNode, parentNode, viewConfigAnnotations);
        }
        else
        {
            viewConfigNode = new PageViewConfigNode(
                    (Class<? extends ViewConfig>) idOfNewNode, parentNode, viewConfigAnnotations);
        }

        parentNode.getChildren().add(viewConfigNode);
        return viewConfigNode;
    }

    public ViewConfigNode findNode(Class nodeClass)
    {
        if (nodeClass == null)
        {
            return null;
        }

        List<Class> path = ViewConfigUtils.toNodeList(nodeClass);

        ViewConfigNode currentNode = this.rootViewConfigNode;

    next:
        for (int i = 0; i < path.size(); i++)
        {
            Class nodeId = path.get(i);

            for (ViewConfigNode node : currentNode.getChildren())
            {
                if (node.getSource().equals(nodeId))
                {
                    currentNode = node;
                    if (i == (path.size() - 1))
                    {
                        return currentNode;
                    }
                    continue next;
                }
            }
            return null;
        }
        return null;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void buildViewConfig(@Observes AfterDeploymentValidation adv)
    {
        if (!isActivated)
        {
            return;
        }

        //needed to transform the metadata-tree during the bootstrapping process
        transformMetaDataTree();
        this.transformed = true;
    }

    protected void transformMetaDataTree()
    {
        if (!this.isActivated)
        {
            return;
        }

        if (this.viewConfigResolver == null)
        {
            ConfigNodeConverter configNodeConverter = new DefaultConfigNodeConverter();
            ViewConfigInheritanceStrategy inheritanceStrategy = new DefaultViewConfigInheritanceStrategy();

            for (Annotation annotation : this.rootViewConfigNode.getMetaData())
            {
                if (annotation.annotationType().equals(ViewConfigRoot.class))
                {
                    ViewConfigRoot viewConfigRoot = (ViewConfigRoot) annotation;

                    configNodeConverter = createCustomConfigNodeConverter(viewConfigRoot, configNodeConverter);
                    inheritanceStrategy = createCustomInheritanceStrategy(viewConfigRoot, inheritanceStrategy);

                    this.viewConfigResolver =
                            createCustomViewConfigResolver(viewConfigRoot, configNodeConverter, inheritanceStrategy);
                    break;
                }
            }

            if (this.viewConfigResolver == null)
            {
                this.viewConfigResolver = new DefaultViewConfigResolver(
                        this.rootViewConfigNode, configNodeConverter, inheritanceStrategy);
            }
            resetRootNode();
        }
    }

    private ViewConfigResolver createCustomViewConfigResolver(ViewConfigRoot viewConfigRoot,
                                                              ConfigNodeConverter configNodeConverter,
                                                              ViewConfigInheritanceStrategy inheritanceStrategy)
    {
        Class<? extends ViewConfigResolver> viewConfigResolverClass = viewConfigRoot.viewConfigResolver();
        if (!ViewConfigResolver.class.equals(viewConfigResolverClass))
        {
            try
            {
                Constructor<? extends ViewConfigResolver> viewConfigResolverConstructor = viewConfigResolverClass
                        .getConstructor(new Class[]{
                            ViewConfigNode.class, ConfigNodeConverter.class, ViewConfigInheritanceStrategy.class});

                return viewConfigResolverConstructor
                        .newInstance(this.rootViewConfigNode, configNodeConverter, inheritanceStrategy);
            }
            catch (Exception e)
            {
                ExceptionUtils.throwAsRuntimeException(e);
            }
        }
        return null;
    }

    private ConfigNodeConverter createCustomConfigNodeConverter(ViewConfigRoot viewConfigRoot,
                                                                ConfigNodeConverter defaultConverter)
    {
        Class<? extends ConfigNodeConverter> converterClass = viewConfigRoot.configNodeConverter();

        if (!ConfigNodeConverter.class.equals(converterClass))
        {
            try
            {
                return converterClass.newInstance();
            }
            catch (Exception e)
            {
                ExceptionUtils.throwAsRuntimeException(e);
            }
        }
        return defaultConverter;
    }

    private ViewConfigInheritanceStrategy createCustomInheritanceStrategy(ViewConfigRoot viewConfigRoot,
                                                                          ViewConfigInheritanceStrategy defaultStrategy)
    {
        Class<? extends ViewConfigInheritanceStrategy> strategyClass = viewConfigRoot.viewConfigInheritanceStrategy();

        if (!ViewConfigInheritanceStrategy.class.equals(strategyClass))
        {
            try
            {
                return strategyClass.newInstance();
            }
            catch (Exception e)
            {
                ExceptionUtils.throwAsRuntimeException(e);
            }
        }
        return defaultStrategy;
    }

    public void freeViewConfigCache(@Observes BeforeShutdown bs)
    {
        this.viewConfigResolver = null;
        this.transformed = false;
    }

    private void resetRootNode()
    {
        this.rootViewConfigNode = new FolderConfigNode(null, null, new HashSet<Annotation>());
    }

    boolean isActivated()
    {
        return isActivated;
    }

    boolean isTransformed()
    {
        return transformed;
    }

    ViewConfigResolver getViewConfigResolver()
    {
        return viewConfigResolver;
    }
}
