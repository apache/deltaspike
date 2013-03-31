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
package org.apache.deltaspike.jsf.impl.config.view.navigation;

import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;
import org.apache.deltaspike.jsf.impl.util.RequestParameter;

import javax.faces.application.NavigationCase;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collection;

/**
 * Destructive operations aren't supported (compared to the SubKeyMap used in MyFaces).
 * Reason: It isn't allowed to remove navigation cases
 * (which are based on {@link org.apache.deltaspike.core.api.config.view.ViewConfig})
 */
public class NavigationCaseMapWrapper implements Map<String, Set<NavigationCase>>
{
    private Map<String, Set<NavigationCase>> wrappedNavigationCaseMap;
    private final Map<String, Set<NavigationCase>> viewConfigBasedNavigationCaseCache;

    /**
     * Constructor for wrapping the given navigation-cases
     *
     * @param navigationCases current navigation-cases
     */
    public NavigationCaseMapWrapper(Map<String, Set<NavigationCase>> navigationCases)
    {
        this.wrappedNavigationCaseMap = navigationCases;
        this.viewConfigBasedNavigationCaseCache = createViewConfigBasedNavigationCases(false);
    }

    private Map<String, Set<NavigationCase>> createViewConfigBasedNavigationCases(boolean allowParameters)
    {
        Map<String, Set<NavigationCase>> result = new HashMap<String, Set<NavigationCase>>();

        Collection<ViewConfigDescriptor> viewConfigDescriptors =
                BeanProvider.getContextualReference(ViewConfigResolver.class).getViewConfigDescriptors();

        if (!viewConfigDescriptors.isEmpty())
        {
            Set<NavigationCase> navigationCase = new HashSet<NavigationCase>();

            Map<String, List<String>> parameters = null;

            if (allowParameters)
            {
                parameters = resolveParameters();
            }

            boolean includeParameters;

            for (ViewConfigDescriptor entry : viewConfigDescriptors)
            {
                View viewMetaData = entry.getMetaData(View.class).iterator().next();
                includeParameters = View.ViewParameterMode.INCLUDE
                        .equals(viewMetaData.viewParams());

                navigationCase.add(new NavigationCase("*",
                        null,
                        null,
                        null,
                        entry.getViewId(),
                        includeParameters ? parameters : null,
                        View.NavigationMode.REDIRECT.equals(viewMetaData.navigation()),
                        includeParameters));

                result.put(entry.getViewId(), navigationCase);
            }
        }
        return result;
    }

    private Map<String, List<String>> resolveParameters()
    {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();

        for (RequestParameter parameter : JsfUtils.getViewConfigPageParameters())
        {
            parameters.put(parameter.getKey(), parameter.getValueList());
        }

        return parameters;
    }

    /**
     * @return the final size (might be a combination of the configured navigation cases (via XML) and the
     *         {@link org.apache.deltaspike.core.api.config.view.ViewConfig}s
     */
    @Override
    public int size()
    {
        return this.wrappedNavigationCaseMap.size() + this.viewConfigBasedNavigationCaseCache.size();
    }

    @Override
    public boolean isEmpty()
    {
        return this.wrappedNavigationCaseMap.isEmpty() &&
                this.viewConfigBasedNavigationCaseCache.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return this.wrappedNavigationCaseMap.containsKey(key) ||
                this.viewConfigBasedNavigationCaseCache.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return this.wrappedNavigationCaseMap.containsValue(value) ||
                this.viewConfigBasedNavigationCaseCache.containsValue(value);
    }

    /**
     * XML configuration overrules {@link org.apache.deltaspike.core.api.config.view.ViewConfig}s
     */
    @Override
    public Set<NavigationCase> get(Object key)
    {
        Set<NavigationCase> result = this.wrappedNavigationCaseMap.get(key);

        if (result == null)
        {
            return createViewConfigBasedNavigationCases(true).get(key);
        }
        return result;
    }

    @Override
    public Set<NavigationCase> put(String key, Set<NavigationCase> value)
    {
        return this.wrappedNavigationCaseMap.put(key, value);
    }

    @Override
    public Set<NavigationCase> remove(Object key)
    {
        return this.wrappedNavigationCaseMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Set<NavigationCase>> m)
    {
        this.wrappedNavigationCaseMap.putAll(m);
    }

    @Override
    public void clear()
    {
        this.wrappedNavigationCaseMap.clear();
    }

    /**
     * @return a combination of navigation-cases configured via XML and
     *         {@link org.apache.deltaspike.core.api.config.view.ViewConfig}s
     */
    @Override
    public Set<String> keySet()
    {
        Set<String> result = new HashSet<String>();
        result.addAll(this.wrappedNavigationCaseMap.keySet());
        result.addAll(this.viewConfigBasedNavigationCaseCache.keySet());
        return result;
    }

    /**
     * @return a combination of navigation-cases configured via XML and
     *         {@link org.apache.deltaspike.core.api.config.view.ViewConfig}s
     */
    @Override
    public Collection<Set<NavigationCase>> values()
    {
        Collection<Set<NavigationCase>> result = new HashSet<Set<NavigationCase>>();

        result.addAll(this.wrappedNavigationCaseMap.values());
        result.addAll(createViewConfigBasedNavigationCases(true).values());
        return result;
    }

    /**
     * @return a combination of navigation-cases configured via XML and
     *         {@link org.apache.deltaspike.core.api.config.view.ViewConfig}s
     */
    @Override
    public Set<Entry<String, Set<NavigationCase>>> entrySet()
    {
        Set<Entry<String, Set<NavigationCase>>> result = new HashSet<Entry<String, Set<NavigationCase>>>();

        result.addAll(this.wrappedNavigationCaseMap.entrySet());
        result.addAll(createViewConfigBasedNavigationCases(true).entrySet());
        return result;
    }
}
