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

import org.apache.deltaspike.core.spi.config.view.InlineMetaDataTransformer;
import org.apache.deltaspike.core.spi.config.view.TargetViewConfigProvider;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Provides the ability to apply metadata to a view-config "remotely" &ndash; from a
 * different place than the view-config itself (and with different syntax and a different annotation).
 *
 * <p>
 * <b>For example</b>, the @ViewControllerRef (main) vs. @ViewRef (inline) &ndash; the @ViewControllerRef is applied
 * directly on a view-config and references a view-controller, but there's also @ViewRef, which has the same purpose,
 * but is applied in reverse &ndash; on a view-controller, referencing a view-config.
 * </p>
 */

@Target({ ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented

public @interface InlineViewMetaData
{
    Class<? extends TargetViewConfigProvider<?>> targetViewConfigProvider();

    @SuppressWarnings("rawtypes")
    Class<? extends InlineMetaDataTransformer> inlineMetaDataTransformer() default InlineMetaDataTransformer.class;
}
