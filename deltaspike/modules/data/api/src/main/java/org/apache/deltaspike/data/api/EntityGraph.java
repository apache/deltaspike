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
package org.apache.deltaspike.data.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an entity graph to be applied to a query. This annotation can be added to any query
 * method of a repository class.
 * <p>
 * The arguments {@code value} and {@code paths} are mutually exclusive. If {@code value} is set, it
 * references a named entity graph defined by JPA metadata.
 * <p>
 * If {@code paths} is set, an entity graph is constructed programmatically from the list of
 * attribute paths.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EntityGraph
{
    /**
     * Name of a named entity graph.
     * @return graph name
     */
    String value() default "";

    /**
     * Type of entity graph (fetch or load).
     * @return graph type
     */
    EntityGraphType type() default EntityGraphType.FETCH;

    /**
     * List of attribute paths. Each path may have multiple components, separated
     * by dots. A single component path adds an attribute node to the entity graph.
     * A path {@code foo.bar.baz} adds an attribute node {@code baz} to a subgraph
     * {@code bar} for the subgraph {@code foo}.
     * 
     * @return list of paths
     */
    String[] paths() default { };
}
