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
package org.apache.deltaspike.jsf.impl.resource;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;

/**
 * {@link ResourceHandlerWrapper} to deliver uncompressed resources in {@link ProjectStage#Development}.
 */
public class DeltaSpikeResourceHandler extends ResourceHandlerWrapper implements Deactivatable
{
    private static final String LIBRARY = "deltaspike";
    private static final String LIBRARY_UNCOMPRESSED = "deltaspike-uncompressed";
    
    private final ResourceHandler wrapped;
    private final String version;
    private final boolean activated;

    public DeltaSpikeResourceHandler(ResourceHandler resourceHandler)
    {
        super();

        wrapped = resourceHandler;
        version = ClassUtils.getJarVersion(this.getClass());
        activated = ClassDeactivationUtils.isActivated(this.getClass());
    }

    @Override
    public Resource createResource(String resourceName, String libraryName)
    {
        Resource resource = wrapped.createResource(resourceName, libraryName);

        if (activated && resource != null && libraryName != null && LIBRARY.equals(libraryName))
        {
            if (ProjectStageProducer.getInstance().getProjectStage() == ProjectStage.Development)
            {
                resource = wrapped.createResource(resourceName, LIBRARY_UNCOMPRESSED);
            }
            
            resource = new DeltaSpikeResource(resource, version);
        }

        return resource;
    }

    @Override
    public ResourceHandler getWrapped()
    {
        return wrapped;
    }
}
