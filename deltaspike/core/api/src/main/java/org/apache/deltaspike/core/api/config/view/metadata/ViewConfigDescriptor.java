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

/**
 * Descriptor which represents a concrete view (page).
 */
public interface ViewConfigDescriptor extends ConfigDescriptor<ViewConfig>
{
    /**
     * View ID of the current descriptor. The default implementation returns the same as ConfigDescriptor#getPath. For
     * the default implementation (and default integration with JSF) it's in place to provide a straightforward API.
     * However, e.g. an integration for a different view technology can use it e.g. for a logical id.
     *
     * @return current view ID
     */
    String getViewId();
}
