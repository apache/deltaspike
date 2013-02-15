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

import org.apache.deltaspike.core.api.config.view.DefaultErrorView;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.spi.config.view.ConfigNodeConverter;
import org.apache.deltaspike.core.spi.config.view.ViewConfigInheritanceStrategy;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.api.literal.FolderLiteral;
import org.apache.deltaspike.jsf.api.literal.ViewLiteral;
import org.apache.deltaspike.jsf.impl.util.ViewConfigUtils;

import javax.enterprise.inject.Typed;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

@Typed()
public class DefaultViewConfigResolver implements ViewConfigResolver
{
    private Map<String, ViewConfigDescriptor> viewIdToViewDefinitionEntryMapping =
        new ConcurrentHashMap<String, ViewConfigDescriptor>();
    private Map<Class<? extends ViewConfig>, ViewConfigDescriptor> viewDefinitionToViewDefinitionEntryMapping;
    private Map<Class, ConfigDescriptor> folderDefinitionToViewDefinitionEntryMapping;
    private Map<String, ConfigDescriptor> folderPathToViewDefinitionEntryMapping;
    private ViewConfigDescriptor defaultErrorView;

    public DefaultViewConfigResolver(ViewConfigNode rootViewConfigNode,
                                     ConfigNodeConverter configNodeConverter,
                                     ViewConfigInheritanceStrategy inheritanceStrategy)
    {
        Map<Class<? extends ViewConfig>, ViewConfigDescriptor> viewConfigs =
            new HashMap<Class<? extends ViewConfig>, ViewConfigDescriptor>();
        Map<Class, ConfigDescriptor> folderConfigs =
            new HashMap<Class, ConfigDescriptor>();

        Map<String, Class<? extends ViewConfig>> foundViewIds = new HashMap<String, Class<? extends ViewConfig>>();

        Stack<ViewConfigNode> nodesToConvert = new Stack<ViewConfigNode>();

        nodesToConvert.addAll(rootViewConfigNode.getChildren());

        while (!nodesToConvert.empty())
        {
            ViewConfigNode currentNode = nodesToConvert.pop();

            //e.g. @View is optional for users, but required from other parts of DeltaSpike -> ensure that it's in place
            addOptionalMetaDataToConfig(currentNode);

            currentNode.getInheritedMetaData().addAll(inheritanceStrategy.resolveInheritedMetaData(currentNode));
            ConfigDescriptor currentConfigDescriptor = configNodeConverter.convert(currentNode);

            if (currentConfigDescriptor instanceof ViewConfigDescriptor)
            {
                ViewConfigDescriptor currentViewConfigDescriptor = (ViewConfigDescriptor) currentConfigDescriptor;

                if (foundViewIds.containsKey(currentViewConfigDescriptor.getViewId()))
                {
                    throw new IllegalStateException(currentViewConfigDescriptor.getViewId() + " is configured twice. " +
                        "That isn't allowed - see: " + currentConfigDescriptor.getConfigClass().getName() + " and " +
                        foundViewIds.get(currentViewConfigDescriptor.getViewId()).getName());
                }
                else
                {
                    foundViewIds.put(
                        currentViewConfigDescriptor.getViewId(), currentViewConfigDescriptor.getViewConfig());
                }

                if (this.defaultErrorView == null)
                {
                    if (DefaultErrorView.class.isAssignableFrom(currentViewConfigDescriptor.getViewConfig()))
                    {
                        this.defaultErrorView = currentViewConfigDescriptor;
                    }
                }
                else if (DefaultErrorView.class.isAssignableFrom(currentViewConfigDescriptor.getViewConfig()))
                {
                    throw new IllegalStateException("It isn't allowed to configure multiple default-error-views. " +
                        "Found default-error-views: " + this.defaultErrorView.getViewConfig() + " and " +
                        currentViewConfigDescriptor.getViewConfig().getName());
                }

                if (!viewConfigs.containsKey(currentViewConfigDescriptor.getViewConfig()))
                {
                    viewConfigs.put(currentViewConfigDescriptor.getViewConfig(), currentViewConfigDescriptor);
                }
            }
            else
            {
                if (!folderConfigs.containsKey(currentConfigDescriptor.getConfigClass()))
                {
                    folderConfigs.put(currentConfigDescriptor.getConfigClass(), currentConfigDescriptor);
                }
            }

            nodesToConvert.addAll(currentNode.getChildren());
        }

        this.viewDefinitionToViewDefinitionEntryMapping = Collections.unmodifiableMap(viewConfigs);
        this.folderDefinitionToViewDefinitionEntryMapping = Collections.unmodifiableMap(folderConfigs);

        initCaches();
    }

    protected void addOptionalMetaDataToConfig(ViewConfigNode currentNode)
    {
        Class sourceClass = currentNode.getSource();
        if (ViewConfigUtils.isFolderConfig(sourceClass))
        {
            for (Annotation annotation : currentNode.getMetaData())
            {
                if (annotation.annotationType().equals(Folder.class))
                {
                    return;
                }
            }
            currentNode.getMetaData().add(new FolderLiteral(true));
            return;
        }

        for (Annotation annotation : currentNode.getMetaData())
        {
            if (annotation.annotationType().equals(View.class))
            {
                return;
            }
        }

        currentNode.getMetaData().add(new ViewLiteral(true));
    }

    @Override
    public ConfigDescriptor getConfigDescriptor(String path)
    {
        if (path == null)
        {
            return null;
        }

        ConfigDescriptor result = this.folderPathToViewDefinitionEntryMapping.get(path);

        if (result == null)
        {
            result = getViewConfigDescriptor(path); //TODO re-visit it
        }

        return result;
    }

    @Override
    public ViewConfigDescriptor getViewConfigDescriptor(String viewId)
    {
        if (viewId == null)
        {
            return null;
        }

        return this.viewIdToViewDefinitionEntryMapping.get(viewId);
    }

    @Override
    public ConfigDescriptor getConfigDescriptor(Class configClass)
    {
        ConfigDescriptor result = null;
        if (ViewConfig.class.isAssignableFrom(configClass))
        {
            result = getViewConfigDescriptor(configClass);
        }

        if (result == null)
        {
            result = this.folderDefinitionToViewDefinitionEntryMapping.get(configClass);
        }

        return result;
    }

    @Override
    public List<ConfigDescriptor> getConfigDescriptors()
    {
        ConfigDescriptor[] result = this.folderDefinitionToViewDefinitionEntryMapping.values()
                .toArray(new ConfigDescriptor[this.folderDefinitionToViewDefinitionEntryMapping.size()]);

        return new ArrayList<ConfigDescriptor>(Arrays.asList(result));
    }

    @Override
    public ViewConfigDescriptor getViewConfigDescriptor(Class<? extends ViewConfig> viewDefinitionClass)
    {
        if (DefaultErrorView.class.equals(viewDefinitionClass))
        {
            return getDefaultErrorViewConfigDescriptor();
        }
        return this.viewDefinitionToViewDefinitionEntryMapping.get(viewDefinitionClass);
    }

    @Override
    public List<ViewConfigDescriptor> getViewConfigDescriptors()
    {
        ViewConfigDescriptor[] result = this.viewDefinitionToViewDefinitionEntryMapping.values()
                .toArray(new ViewConfigDescriptor[this.viewDefinitionToViewDefinitionEntryMapping.size()]);

        return new ArrayList<ViewConfigDescriptor>(Arrays.asList(result));
    }

    @Override
    public ViewConfigDescriptor getDefaultErrorViewConfigDescriptor()
    {
        return this.defaultErrorView;
    }

    protected void initCaches()
    {
        //folders
        Map<String, ConfigDescriptor> folderPathMapping = new HashMap<String, ConfigDescriptor>();
        for (ConfigDescriptor configDescriptor : this.folderDefinitionToViewDefinitionEntryMapping.values())
        {
            if (folderPathMapping.containsKey(configDescriptor.toString()))
            {
                throw new IllegalStateException("Duplicated config for the same folder configured. See: " +
                    folderPathMapping.get(
                            configDescriptor.toString()).getConfigClass().getName() +
                    " and " + configDescriptor.getConfigClass().getName());
            }
            folderPathMapping.put(configDescriptor.toString(), configDescriptor);
        }
        this.folderPathToViewDefinitionEntryMapping = Collections.unmodifiableMap(folderPathMapping);

        //pages
        for (ViewConfigDescriptor viewConfigDescriptor : this.viewDefinitionToViewDefinitionEntryMapping.values())
        {
            if (viewIdToViewDefinitionEntryMapping.containsKey(viewConfigDescriptor.getViewId()))
            {
                throw new IllegalStateException("Duplicated config for the same page configured. See: " +
                    viewIdToViewDefinitionEntryMapping.get(
                            viewConfigDescriptor.getViewId()).getConfigClass().getName() +
                    " and " + viewConfigDescriptor.getConfigClass().getName());
            }
            this.viewIdToViewDefinitionEntryMapping.put(viewConfigDescriptor.getViewId(), viewConfigDescriptor);
        }
    }
}
