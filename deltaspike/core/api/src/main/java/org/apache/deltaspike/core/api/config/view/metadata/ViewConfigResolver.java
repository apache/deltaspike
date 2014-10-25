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
package org.apache.deltaspike.core.api.config.view.metadata;

import org.apache.deltaspike.core.api.config.view.ViewConfig;

import java.util.List;

/**
 * Resolver of view-configs.
 *
 * A {@link ConfigDescriptor} can be bound to any config class (without required base type). That's needed e.g. for
 * folder-configs. Whereas {@link ViewConfigDescriptor}s only represent classes which inherit from {@link ViewConfig}
 * which is required for all view-configs.
 *
 * Use {@link org.apache.deltaspike.core.spi.config.view.ViewConfigRoot} to register a custom resolver.
 */
//TODO re-visit name since we also need ConfigDescriptor
public interface ViewConfigResolver
{
    ConfigDescriptor<?> getConfigDescriptor(String path);

    /**
     * Resolves the {@link ConfigDescriptor} for the given class.
     *
     * @param configClass config class which usually represents a folder node
     *
     * @return config descriptor which represents the given config class
     */
    ConfigDescriptor<?> getConfigDescriptor(Class<?> configClass);

    //TODO re-visit name (depends on other discussions)
    /**
     * Resolves all descriptors for folders.
     *
     * @return all descriptors for the known folder-configs
     */
    List<ConfigDescriptor<?>> getConfigDescriptors();

    /**
     * Resolves the {@link ViewConfigDescriptor} for the given view-id.
     *
     * @param viewId view-id of the page
     *
     * @return view-config descriptor which represents the given view-id, null otherwise
     */
    ViewConfigDescriptor getViewConfigDescriptor(String viewId);

    /**
     * Resolves the {@link ViewConfigDescriptor} for the given view-config class.
     *
     * @param viewDefinitionClass view-config class of the page
     *
     * @return view-config descriptor which represents the given view-config class
     */
    ViewConfigDescriptor getViewConfigDescriptor(Class<? extends ViewConfig> viewDefinitionClass);

    /**
     * Resolves all descriptors for the known {@link ViewConfig}s.
     *
     * @return all descriptors for the known view-configs
     */
    List<ViewConfigDescriptor> getViewConfigDescriptors();

    /**
     * Resolves the descriptor for the default error page.
     *
     * @return descriptor for the default error page
     */
    ViewConfigDescriptor getDefaultErrorViewConfigDescriptor();
}
