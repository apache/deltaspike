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
import java.util.List;

/**
 * Allows to customize the inheritance-strategy for meta-data.
 * E.g. inheritance via std. java inheritance vs. inheritance via nested interfaces.
 * Use {@link ViewConfigRoot} to configure a custom inheritance-strategy.
 */
public interface ViewConfigInheritanceStrategy
{
    /**
     * @param viewConfigNode current view-config node
     * @return annotation instances which should be merged with the annotation instances of the node itself
     */
    List<Annotation> resolveInheritedMetaData(ViewConfigNode viewConfigNode);
}
