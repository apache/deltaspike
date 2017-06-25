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
package org.apache.deltaspike.jpa.api.transaction;

import javax.enterprise.inject.Any;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If it isn't possible to use EJBs, this interceptor adds transaction support to methods or a class.
 * The optional qualifier can be used to specify different entity managers.
 * <p/>
 * Further details can be found at {@link TransactionScoped} which is an optional scope which can be used together with
 * &#064;Transactional.
 */

@InterceptorBinding
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Transactional
{
    /**
     * Optional qualifier/s which allow/s to start only specific transactions instead of one
     * for the injected {@link javax.persistence.EntityManager}s.
     * Default-value is {@link Any} which means any injected {@link javax.persistence.EntityManager}s
     * should be detected automatically and transactions for all injected {@link javax.persistence.EntityManager}s
     * will be started. Or the {@link javax.enterprise.inject.Default} {@link javax.persistence.EntityManager}
     * will be used, if no qualifier and no {@link javax.persistence.EntityManager} was found (in the annotated class)
     * (see DELTASPIKE-320).
     *
     * This qualifier can also be used for integrating other frameworks,
     * which follow a different style (see DELTASPIKE-319) as well as the usage of
     * {@link javax.persistence.EntityManager}s with qualifiers in a called method (of a different bean)
     * which isn't {@link Transactional} itself.
     *
     * This method is now deprecated, and if you have multiple {@link javax.persistence.EntityManager}s you should use
     * {@link org.apache.deltaspike.jpa.api.entitymanager.EntityManagerResolver}.
     * If you want to use qualifiers only, use {@link org.apache.deltaspike.jpa.api.entitymanager.EntityManagerConfig}.
     *
     * @return target persistence-unit identifier
     */
    @Deprecated
    @Nonbinding Class<? extends Annotation>[] qualifier() default Any.class;

    /**
     * Only evaluated on the first/outermost transactional bean/method in the chain
     * @return true to trigger #rollback for the current transaction(s), false otherwise
     */
    @Nonbinding boolean readOnly() default false;
}
