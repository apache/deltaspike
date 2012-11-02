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
package org.apache.deltaspike.jsf.api.config.view;

import org.apache.deltaspike.core.api.config.view.metadata.annotation.Aggregated;
import org.apache.deltaspike.core.api.config.view.metadata.annotation.ViewMetaData;

import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation can be used as interceptor for JSF action methods as an alternative for
 * {@link org.apache.deltaspike.jsf.api.navigation.PageParameterContext}.
 */
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
@Documented

@ViewMetaData
@Aggregated(true)
//TODO @InterceptorBinding
public @interface PageParameter
{
    /**
     * Key of the parameter
     *
     * @return name of the key
     */
    @Nonbinding String key();

    /**
     * Value or EL-Expression of the parameter
     *
     * @return ref or expression
     */
    @Nonbinding String value();

    @Target({ METHOD, TYPE })
    @Retention(RUNTIME)
    @Documented

    //TODO add special support for list-annotations (add value automatically)
    /**
     * Allows to specify multiple parameters (@see ViewParameter)
     */
    @ViewMetaData
    @Aggregated(true)
    //TODO @InterceptorBinding
    public static @interface List
    {
        /**
         * 1-n parameters
         *
         * @return parameters
         */
        @Nonbinding PageParameter[] value();
    }
}
