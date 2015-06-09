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

import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameter;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;
import org.apache.deltaspike.jsf.spi.config.view.navigation.NavigationParameterStrategy;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Dependent
public class DefaultNavigationParameterStrategy implements NavigationParameterStrategy
{
    private static final long serialVersionUID = 198321901578229292L;

    @Inject
    private NavigationParameterContext navigationParameterContext;

    @Override
    public Object execute(InvocationContext ic) throws Exception
    {
        List<NavigationParameter> parameterList = new ArrayList<NavigationParameter>();

        NavigationParameter navigationParameter = ic.getMethod().getAnnotation(NavigationParameter.class);

        if (navigationParameter != null)
        {
            parameterList.add(navigationParameter);
        }

        NavigationParameter.List navigationParameterList = ic.getMethod().getAnnotation(NavigationParameter.List.class);

        if (navigationParameterList != null)
        {
            Collections.addAll(parameterList, navigationParameterList.value());
        }

        for (NavigationParameter currentParameter : parameterList)
        {
            JsfUtils.addStaticNavigationParameter(
                this.navigationParameterContext, currentParameter.key(), currentParameter.value());
        }

        return ic.proceed();
    }
}
