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
package org.apache.deltaspike.jsf.impl.config.view;

import org.apache.deltaspike.core.api.config.view.metadata.CallbackDescriptor;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FolderConfigNode extends AbstractConfigNode
{
    //not all interfaces have to implement the ViewConfig interface
    private final Class<?> nodeId;

    public FolderConfigNode(Class<?> nodeId, ViewConfigNode parent, Set<Annotation> nodeMetaData)
    {
        super(parent, nodeMetaData);
        this.nodeId = nodeId;
    }

    public FolderConfigNode(ViewConfigNode nodeToCopy, Class viewConfigClass)
    {
        super(nodeToCopy.getParent(), nodeToCopy.getMetaData());
        getInheritedMetaData().addAll(nodeToCopy.getInheritedMetaData());
        getChildren().addAll(nodeToCopy.getChildren());

        for (Map.Entry<Class<? extends Annotation>, List<CallbackDescriptor>> callbackDescriptorEntry :
                nodeToCopy.getCallbackDescriptors().entrySet())
        {
            for (CallbackDescriptor callbackDescriptor : callbackDescriptorEntry.getValue())
            {
                registerCallbackDescriptors(callbackDescriptorEntry.getKey(), callbackDescriptor);
            }
        }
        this.nodeId = viewConfigClass;
    }

    @Override
    public Class<?> getSource()
    {
        return this.nodeId;
    }
}
