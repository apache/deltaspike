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
package org.apache.deltaspike.core.api.config.view.metadata;

import org.apache.deltaspike.core.spi.config.view.ConfigPreProcessor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This meta-annotation allows to create custom meta-data which can be used for view-configs.
 * Per default meta-data of a lower level overrides meta-data on a higher level which has the same type.
 * Can be customized via annotating the final annotation as a whole via @Aggregated(true) or only special fields of it.
 */
@Target({ ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented

@Aggregated(false)
public @interface ViewMetaData
{
    Class<? extends ConfigPreProcessor> preProcessor() default ConfigPreProcessor.class;
}
