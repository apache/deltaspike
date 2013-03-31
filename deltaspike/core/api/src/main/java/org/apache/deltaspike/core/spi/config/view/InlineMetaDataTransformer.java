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
package org.apache.deltaspike.core.spi.config.view;

import java.lang.annotation.Annotation;

/**
 * Allows to transform an annotation annotated with @InlineViewMetaData to an annotation annotated with @ViewMetaData.
 * This transformer is optional and only needed if it should result in the same at runtime, but the inline-meta-data
 * needs a different syntax via a different annotation (compared to the view-config meta-data).
 * E.g. see @ViewRef vs. @ViewControllerRef.
 *
 * @param <I> type of the inline-meta-data
 * @param <T> type of the target-meta-data
 */
public interface InlineMetaDataTransformer
        <I extends Annotation /*inline metadata*/, T extends Annotation /*target metadata*/>
{
    T convertToViewMetaData(I inlineMetaData, Class<?> sourceClass);
}
