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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.parameter.uc004;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class PageBean004 implements Serializable
{
    private static final long serialVersionUID = -436742087379474804L;

    private int currentValue = -2;

    @Inject
    private NavigationParameterContext navigationParameterContext;

    public Class<? extends ViewConfig> actionMethod()
    {
        currentValue++;

        if (currentValue >= 0)
        {
            this.navigationParameterContext.addPageParameter("cv", this.currentValue);
        }
        return SimplePageConfig.class;
    }
}
