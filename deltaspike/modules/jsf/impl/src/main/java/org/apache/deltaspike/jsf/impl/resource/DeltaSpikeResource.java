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
import javax.faces.application.ResourceWrapper;

/**
 * {@link ResourceWrapper} which appends the version of DeltaSpike to the URL.
 */
public class DeltaSpikeResource extends ResourceWrapper
{

    private final Resource wrapped;
    private final String version;

    public DeltaSpikeResource(Resource resource, String version)
    {
        super();
        this.wrapped = resource;
        this.version = version;
    }

    @Override
    public Resource getWrapped()
    {
        return wrapped;
    }

    @Override
    public String getRequestPath()
    {
        return super.getRequestPath() + "&v=" + version;
    }

    @Override
    public String getContentType()
    {
        return getWrapped().getContentType();
    }

    @Override
    public String getLibraryName()
    {
        return getWrapped().getLibraryName();
    }

    @Override
    public String getResourceName()
    {
        return getWrapped().getResourceName();
    }

    @Override
    public void setContentType(String contentType)
    {
        getWrapped().setContentType(contentType);
    }

    @Override
    public void setLibraryName(String libraryName)
    {
        getWrapped().setLibraryName(libraryName);
    }

    @Override
    public void setResourceName(String resourceName)
    {
        getWrapped().setResourceName(resourceName);
    }

    @Override
    public String toString()
    {
        return getWrapped().toString();
    }
}
