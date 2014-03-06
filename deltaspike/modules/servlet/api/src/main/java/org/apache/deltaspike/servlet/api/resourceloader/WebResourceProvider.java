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
package org.apache.deltaspike.servlet.api.resourceloader;

import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.deltaspike.core.api.resourceloader.AbstractResourceProvider;
import org.apache.deltaspike.core.api.resourceloader.InjectableResource;

/**
 * Loads resources using {@link ServletContext#getResource(String)}.
 */
@ApplicationScoped
public class WebResourceProvider extends AbstractResourceProvider
{
    @Inject
    @DeltaSpike
    private ServletContext servletContext;

    @Override
    public InputStream readStream(InjectableResource injectableResource)
    {
        /*
         * ServletContext.getResourceAsStream() requires the path to start with "/". We add it here if it is missing
         * because it is a common mistake to miss it.
         */
        String path = injectableResource.location();
        if (!path.startsWith("/"))
        {
            path = "/" + path;
        }

        return servletContext.getResourceAsStream(path);
    }
}
