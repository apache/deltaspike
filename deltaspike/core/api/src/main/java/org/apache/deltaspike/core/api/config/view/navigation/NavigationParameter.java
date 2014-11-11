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
package org.apache.deltaspike.core.api.config.view.navigation;

import org.apache.deltaspike.core.api.config.view.metadata.Aggregated;
import org.apache.deltaspike.core.api.config.view.metadata.ViewMetaData;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used on JSF action methods, this adds navigation parameters (key-value pairs) to the resulting navigation string.
 * Alternatively, {@link org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext} can be used
 * to add the parameters.
 */
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
@Documented

@ViewMetaData
@Aggregated(true)
@InterceptorBinding
public @interface NavigationParameter
{
    /**
     * Key of the parameter.
     *
     * @return name of the key
     */
    @Nonbinding String key();

    /**
     * Value of the parameter, a plain String or an EL expression.
     *
     * @return ref or expression
     */
    @Nonbinding String value();

    @Target({ METHOD, TYPE })
    @Retention(RUNTIME)
    @Documented

    //TODO add special support for list-annotations (add value automatically)
    /**
     * A container for multiple NavigationParameters.
     */
    @ViewMetaData
    @Aggregated(true)
    @InterceptorBinding
    public static @interface List
    {
        /**
         * One or more navigation parameters.
         *
         * @return parameters
         */
        @Nonbinding NavigationParameter[] value();
    }
}
