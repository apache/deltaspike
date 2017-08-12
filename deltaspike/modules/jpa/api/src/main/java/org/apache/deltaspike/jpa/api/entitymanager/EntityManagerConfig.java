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

package org.apache.deltaspike.jpa.api.entitymanager;

import javax.enterprise.inject.Any;
import javax.persistence.FlushModeType;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configure the EntityManager for a specific repository.
 */
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EntityManagerConfig
{
    /**
     * References the type which provides the EntityManager for a specific repository.
     * Must be resolvable over the BeanManager.
     */
    Class<? extends EntityManagerResolver> entityManagerResolver() default EntityManagerResolver.class;

    /**
     * If no entityManagerResolver is specified, then these qualifiers will be used to look up an entity manager
     * @return
     */
    Class<? extends Annotation>[] qualifier() default Any.class;

    /**
     * Set the flush mode for the repository EntityManager.
     */
    FlushModeType flushMode() default FlushModeType.AUTO;

}
