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
package org.apache.deltaspike.core.api.config.base;

import org.apache.deltaspike.core.api.config.ConfigResolver;

import java.util.concurrent.TimeUnit;

public interface CoreBaseConfig extends DeltaSpikeBaseConfig
{
    interface BeanManagerIntegration
    {
        Boolean DELEGATE_LOOKUP =
                ConfigResolver.resolve("deltaspike.bean-manager.delegate_lookup")
                        .as(Boolean.class)
                        .withCurrentProjectStage(true)
                        .withDefault(Boolean.TRUE)
                        .getValue();
    }

    interface InterceptorCustomization
    {
        Integer PRIORITY =
                ConfigResolver.resolve("deltaspike.interceptor.priority")
                        .as(Integer.class)
                        .withCurrentProjectStage(true)
                        .withDefault(999) //PLATFORM_BEFORE is 0, LIBRARY_BEFORE is 1000 and APPLICATION is 2000
                        .getValue();
    }

    interface Validation
    {
        ViolationMode VIOLATION_MODE =
                ConfigResolver.resolve("deltaspike.validation.violation-mode")
                        .as(ViolationMode.class, new ConfigResolver.Converter<ViolationMode>()
                        {
                            @Override
                            public ViolationMode convert(String value)
                            {
                                return ViolationMode.valueOf(value);
                            }
                        })
                        .withCurrentProjectStage(true)
                        .withDefault(ViolationMode.FAIL)
                        .getValue();

        enum ViolationMode
        {
            IGNORE, WARN, FAIL
        }
    }

    interface MBeanIntegration
    {
        Boolean AUTO_UNREGISTER =
                ConfigResolver.resolve("deltaspike.mbean.auto-unregister")
                        .as(Boolean.class)
                        .withCurrentProjectStage(true)
                        .withDefault(Boolean.TRUE)
                        .getValue();
    }

    interface ScopeCustomization
    {
        interface WindowRestriction
        {
            String MAX_COUNT_KEY = "deltaspike.scope.window.max-count";

            Integer MAX_COUNT =
                    ConfigResolver.resolve(MAX_COUNT_KEY)
                            .as(Integer.class)
                            .withCurrentProjectStage(true)
                            .withDefault(1024)
                            .getValue();
        }
    }

    interface TimeoutCustomization
    {
        Integer FUTUREABLE_TERMINATION_TIMEOUT_IN_MILLISECONDS =
                ConfigResolver.resolve("deltaspike.futureable.termination-timeout_in_milliseconds")
                        .as(Integer.class)
                        .withCurrentProjectStage(true)
                        .withDefault((int) TimeUnit.MINUTES.toMillis(1))
                        .getValue();
    }

    interface ParentExtensionCustomization
    {
        Boolean PARENT_EXTENSION_ENABLED =
                ConfigResolver.resolve("deltaspike.parent.extension.enabled")
                        .as(Boolean.class)
                        .withCurrentProjectStage(true)
                        .withDefault(Boolean.FALSE)
                        .getValue();
    }

    interface InterDynCustomization
    {
        /**
         * All interdyn rules start with this prefix and contains a 'match' and a 'annotation' part.
         * The 'match' is a regular expression which depicts the classes which should get annotated.
         * The 'annotation' is the annotation name which should get applied to all the classes which
         * match the 'match' regexp.
         *
         * A sample config might look like:
         * <pre>
         * deltaspike.interdyn.rule.1.match=com\.mycorp\..*Service.*
         * deltaspike.interdyn.rule.1.annotation=org.apache.deltaspike.core.api.monitoring.InvocationMonitored
         * </pre>
         */
        String INTERDYN_RULE_PREFIX = "deltaspike.interdyn.rule.";

        /**
         * Whether the InterDyn feature is enabled or not.
         *
         * If the feature is enabled at startup then we will apply the interceptors dynamically
         * to all the matching classes.
         * Otherwise we will skip the instrumentation.
         */
        ConfigResolver.TypedResolver<Boolean> INTERDYN_ENABLED =
                ConfigResolver.resolve("deltaspike.interdyn.enabled")
                        .as(Boolean.class)
                        .withCurrentProjectStage(true)
                        .withDefault(Boolean.FALSE);

    }
}
