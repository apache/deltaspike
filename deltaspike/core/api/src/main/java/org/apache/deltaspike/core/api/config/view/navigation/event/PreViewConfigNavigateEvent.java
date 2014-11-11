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
package org.apache.deltaspike.core.api.config.view.navigation.event;

import org.apache.deltaspike.core.api.config.view.ViewConfig;

/**
 * This event is fired before a navigation from/to a view-config-based page occurs. With {@link #navigateTo(Class)} it's
 * possible to change the navigation target.
 */
public class PreViewConfigNavigateEvent
{
    private final Class<? extends ViewConfig> fromView;
    private Class<? extends ViewConfig> toView;

    /**
     * Constructor for creating the event for the given source and target view.
     *
     * @param fromView source-view
     * @param toView   target-view
     */
    public PreViewConfigNavigateEvent(Class<? extends ViewConfig> fromView, Class<? extends ViewConfig> toView)
    {
        this.fromView = fromView;
        this.toView = toView;
    }

    /**
     * Provides the navigation source.
     *
     * @return source of the navigation
     */
    public Class<? extends ViewConfig> getFromView()
    {
        return fromView;
    }

    /**
     * Provides the navigation target.
     *
     * @return target of the navigation
     */
    public Class<? extends ViewConfig> getToView()
    {
        return toView;
    }

    /**
     * Changes the navigation target.
     *
     * @param toView new navigation target
     */
    public void navigateTo(Class<? extends ViewConfig> toView)
    {
        this.toView = toView;
    }
}
