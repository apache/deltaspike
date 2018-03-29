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
 * Allows to change the found meta-data (e.g. replace default values - example:
 * Page.extension needs "" as a default for the meta-data-merging process, but "xhtml" should be the final default)
 * or the {@link ViewConfigNode} itself. E.g. to register callbacks supported by the meta-data
 * (see {@link org.apache.deltaspike.core.api.config.view.metadata.ViewMetaData#preProcessor()} )
 *
 * @param <T> meta-data type
 */
public interface ConfigPreProcessor<T extends Annotation>
{
    /**
     * @param metaData The annotation-instance which was found or the inherited instance.
     *                 Since it's possible to override annotation-attributes alongside the inheritance-path,
     *                 it can be a merged representation.
     *                 To get rid of meta-data which is only inherited,
     *                 it's required to check the presence of the physical annotation e.g. via
     *                 ViewConfigNode#getSource#isAnnotationPresent
     *                 and return a synthetic literal-instance (as a marker/placeholder),
     *                 because 'null' isn't supported as return-value.
     * @param viewConfigNode Instance which represents the current node
     * @return The annotation-instance which should be used for the final meta-data
     */
    T beforeAddToConfig(T metaData, ViewConfigNode viewConfigNode);
}
