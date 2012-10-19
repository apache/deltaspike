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
package org.apache.deltaspike.jsf.api.listener.request;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Target;

/**
 * Qualifier for observers which should be invoked before the current {@link javax.faces.context.FacesContext} gets
 * destroyed.
 * <p/>
 * Parameter-type of the observer: {@link javax.faces.context.FacesContext}
 * <p/>
 * Attention: referencing @ApplicationScoped or @Singleton scoped beans might lead to issues with a CDI implementation
 * (e.g. if the ServletRequestListener of OWB gets called earlier)
 */

@Target({ PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented

@Qualifier
public @interface AfterJsfRequest
{
}