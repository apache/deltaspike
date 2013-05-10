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
package org.apache.deltaspike.core.spi.config.view;

import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Allows to customize the default behaviour for processing the meta-data-tree
 */

@Target(TYPE)
@Retention(RUNTIME)
@Documented

/**
 * Optional annotation which allows to provide custom implementations.
 * Only annotate one {@link org.apache.deltaspike.core.api.config.view.ViewConfig} class which represents the root node.
 */
public @interface ViewConfigRoot
{
    Class<? extends ViewConfigResolver> viewConfigResolver()
        default ViewConfigResolver.class;

    Class<? extends ConfigNodeConverter> configNodeConverter()
        default ConfigNodeConverter.class;

    Class<? extends ViewConfigInheritanceStrategy> viewConfigInheritanceStrategy()
        default ViewConfigInheritanceStrategy.class;

    Class<? extends ConfigDescriptorValidator>[] configDescriptorValidators()
        default { };
}
