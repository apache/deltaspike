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
package org.apache.deltaspike.testcontrol.api;

import org.apache.deltaspike.core.spi.filter.ClassFilter;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Optional control annotation for unit-tests
 */

@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface TestControl
{
    /**
     * only supports contexts supported by ContextControl#startContext
     * defaults: session- and request-scope
     */
    Class<? extends Annotation>[] startScopes() default { };

    //TODO discuss callbacks

    Class<? extends ProjectStage> projectStage() default ProjectStage.UnitTest.class;

    /**
     * only supported on test-class-level
     */
    Class<? extends Handler> logHandler() default ConsoleHandler.class;

    /**
     * Requires additional service-loader config
     * Currently only supported on class-level
     */
    boolean startExternalContainers() default true;

    /**
     * allows to label alternative cdi-beans similar to global alternatives to bind them to 0-n tests
     */
    Class<? extends Label> activeAlternativeLabel() default Label.class;

    //with cdi 1.1+ it can be used to implement labeled-alternatives without text based config
    //(details see DELTASPIKE-1338)
    /**
     * low-level filter (mainly needed for special cases if labeled-alternatives aren't enough)
     * @return the class-filter class which should be used for the current test-class
     */
    Class<? extends ClassFilter> classFilter() default ClassFilter.class;

    interface Label
    {
    }
}
