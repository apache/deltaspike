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
package org.apache.deltaspike.core.api.config.view;

import org.apache.deltaspike.core.api.literal.ViewControllerRefLiteral;
import org.apache.deltaspike.core.api.config.view.metadata.InlineViewMetaData;
import org.apache.deltaspike.core.spi.config.view.InlineMetaDataTransformer;
import org.apache.deltaspike.core.spi.config.view.TargetViewConfigProvider;
import org.apache.deltaspike.core.api.config.view.controller.ViewControllerRef;

import javax.enterprise.util.Nonbinding;
import javax.inject.Named;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A reference to a view-config, applied on a view-controller. The opposite direction of {@link ViewControllerRef}.
 *
 * ViewRef annotation instances are not present at runtime as metadata, they are instead transformed to
 * ViewControllerRef instances during deployment.
 */

@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@Documented

@InlineViewMetaData(
        targetViewConfigProvider = ViewRef.ViewRefTargetViewConfigProvider.class,
        inlineMetaDataTransformer = ViewRef.ViewRefInlineMetaDataTransformer.class)
public @interface ViewRef
{
    abstract class Manual implements ViewConfig
    {
    }

    /**
     * Specifies the views to bind to the view-controller.
     *
     * @return {@link ViewConfig}s of views bound to the view-controller
     */
    @Nonbinding Class<? extends ViewConfig>[] config();

    class ViewRefTargetViewConfigProvider implements TargetViewConfigProvider<ViewRef>
    {
        @Override
        public Class<? extends ViewConfig>[] getTarget(ViewRef inlineMetaData)
        {
            return inlineMetaData.config();
        }
    }

    class ViewRefInlineMetaDataTransformer implements InlineMetaDataTransformer<ViewRef, ViewControllerRef>
    {
        @Override
        public ViewControllerRef convertToViewMetaData(ViewRef inlineMetaData, Class<?> sourceClass)
        {

            Named named = sourceClass.getAnnotation(Named.class);

            String beanName;
            if (named == null)
            {
                beanName = null;
            }
            else
            {
                beanName = named.value();
            }
            return new ViewControllerRefLiteral(sourceClass, beanName);
        }
    }
}
