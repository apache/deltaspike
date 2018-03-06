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
package org.apache.deltaspike.core.impl.config;

class ConfigResolverContext
{
    static final ConfigResolverContext NONE = new ConfigResolverContext();

    static final ConfigResolverContext EVAL_VARIABLES = new ConfigResolverContext().setEvaluateVariables(true);

    static final ConfigResolverContext PROJECTSTAGE = new ConfigResolverContext().setProjectStageAware(true);

    static final ConfigResolverContext PROJECTSTAGE_EVAL_VARIABLES = new ConfigResolverContext()
                                                                            .setProjectStageAware(true)
                                                                            .setEvaluateVariables(true);

    private boolean projectStageAware;
    private boolean evaluateVariables;
    private boolean propertyAware;

    ConfigResolverContext()
    {
    }
    
    ConfigResolverContext setEvaluateVariables(final boolean evaluateVariables)
    {
        this.evaluateVariables = evaluateVariables;
        return this;
    }   
    
    boolean isEvaluateVariables()
    {
        return evaluateVariables;
    }

    ConfigResolverContext setProjectStageAware(final boolean projectStageAware)
    {
        this.projectStageAware = projectStageAware;
        return this;
    }
    
    boolean isProjectStageAware()
    {
        return projectStageAware;
    }

    ConfigResolverContext setPropertyAware(final boolean propertyAware)
    {
        this.propertyAware = propertyAware;
        return this;
    }
    
    boolean isPropertyAware()
    {
        return propertyAware;
    }
}
