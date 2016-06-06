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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

/**
 * Supply query meta data to a method with this annotation.<br/>
 * Currently supports:
 * <ul><li>JPQL queries as part of the annotation value</li>
 * <li>Execute named queries referenced by the named value</li>
 * <li>Execute native SQL queries</li>
 * <li>Restrict the result size to a static value</li>
 * <li>Provide a lock mode</li></ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Query
{

    /**
     * Defines the Query to execute. Can be left empty for method expression queries
     * or when referencing a {@link #named()} query.
     */
    String value() default "";

    /**
     * References a named query.
     */
    String named() default "";

    /**
     * Defines a native SQL query.
     */
    boolean isNative() default false;

    /**
     * Limits the number of results the query returns.
     */
    int max() default 0;

    /**
     * Defines a lock mode for the query.
     */
    LockModeType lock() default LockModeType.NONE;

    /**
     * (Optional) Query properties and hints.  May include vendor-specific query hints.
     */
    QueryHint[] hints() default {
    };

    /**
     * Defines how a single result query is fetched. Defaults to the JPA way with
     * Exceptions thrown on non-single result queries.
     */
    SingleResultType singleResult() default SingleResultType.JPA;
}
