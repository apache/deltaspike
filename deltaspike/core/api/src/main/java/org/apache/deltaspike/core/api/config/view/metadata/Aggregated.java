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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation can be used to mark annotations as aggregated meta-data.
 * Core just provides this annotation, but the concrete behaviour is defined by a concrete ConfigNodeConverter.
 * E.g. DefaultConfigNodeConverter uses the result stored in ViewConfigNode#getInheritedMetaData to replace
 * default- (/ null-) values of "higher" levels with custom values of "lower" levels,
 * if #value is 'true'.
 */
@Target({ ANNOTATION_TYPE }) //TODO re-visit and discuss method-level (for annotation-attributes)
@Retention(RUNTIME)
@Documented
public @interface Aggregated
{
    /**
     * @return false to override the same meta-data type of the parent view-config and
     *         true to allow multiple instances of a meta-data per view
     */
    boolean value();
}
