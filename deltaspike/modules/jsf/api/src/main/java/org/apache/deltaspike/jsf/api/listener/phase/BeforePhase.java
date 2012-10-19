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
package org.apache.deltaspike.jsf.api.listener.phase;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ PARAMETER, FIELD, METHOD })
@Retention(RUNTIME)
@Documented

@Qualifier

/**
 * - for request-observer-methods
 * //TODO - for lifecycle callbacks in view-definitions
 *
 * Parameter-type of the observer: {@link javax.faces.event.PhaseEvent}
 */
public @interface BeforePhase
{
    /**
     * {@link JsfPhaseId} which is the equivalent for the {@link javax.faces.event.PhaseId} value.
     * For more details see {@link JsfPhaseId}
     *
     * @return request-id which defines the jsf-lifecycle-request to completely define this qualifier
     */
    JsfPhaseId value();
}