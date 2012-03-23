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
package org.apache.deltaspike.security.api.authorization.annotation;

import org.apache.deltaspike.security.api.authorization.AccessDecisionVoter;

import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Interceptor for securing beans.
 * It's also possible to use it as meta-annotation for type-safe view-configs.
 */
@Target({ TYPE, METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented

//cdi annotations
@SecurityBindingType
public @interface Secured
{
    /**
     * {@link AccessDecisionVoter}s which will be invoked before accessing the intercepted instance or in case of
     * view-configs before a view gets used.
     *
     * @return the configured access-decision-voters which should be used for the voting process
     */
    @Nonbinding
    Class<? extends AccessDecisionVoter>[] value();
}
