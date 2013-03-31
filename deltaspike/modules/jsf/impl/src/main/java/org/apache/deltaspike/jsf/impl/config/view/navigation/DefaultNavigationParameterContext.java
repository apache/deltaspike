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

import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;

import javax.enterprise.context.RequestScoped;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Request scoped storage for page-parameters.
 * Can be used to add parameters dynamically to the final navigation string
 */
@RequestScoped
public class DefaultNavigationParameterContext implements NavigationParameterContext
{
    private static final long serialVersionUID = 6027959542775130265L;

    private Map<String, String> parameters = new HashMap<String, String>();

    protected DefaultNavigationParameterContext()
    {
    }

    public Map<String, String> getPageParameters()
    {
        return Collections.unmodifiableMap(this.parameters);
    }

    public void addPageParameter(String key, Object param)
    {
        if (param == null)
        {
            this.parameters.remove(key);
            return;
        }

        String value = param.toString().trim();

        if (value.startsWith("#{") && value.endsWith("}"))
        {
            value = JsfUtils.getValueOfExpressionAsString(value);
        }

        //simple version - we could add multi-ref support, if we really need it
        //but this method might be called multiple times for the same parameter/s
        this.parameters.put(key, value);
    }
}
