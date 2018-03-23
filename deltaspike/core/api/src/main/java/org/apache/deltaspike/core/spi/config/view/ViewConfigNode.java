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

import org.apache.deltaspike.core.api.config.view.metadata.CallbackDescriptor;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Node-type used for building the meta-data-tree during the bootstrapping process.
 */
public interface ViewConfigNode
{
    ViewConfigNode getParent();

    List<ViewConfigNode> getChildren();

    Class<?> getSource();

    Set<Annotation> getMetaData();

    List<Annotation> getInheritedMetaData();

    Map<Class<? extends Annotation>, List<CallbackDescriptor>> getCallbackDescriptors();

    //TODO
    List<CallbackDescriptor> getCallbackDescriptors(Class<? extends Annotation> metaDataType);

    void registerCallbackDescriptors(Class<? extends Annotation> metaDataType, CallbackDescriptor callbackDescriptor);
}
