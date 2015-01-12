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

import org.apache.deltaspike.core.spi.config.view.ConfigDescriptorValidator;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

@ApplicationScoped
public class ViewConfigResolverProducer
{
    private static final Logger LOG = Logger.getLogger(ViewConfigResolverProducer.class.getName());

    @Inject
    private ViewConfigExtension viewConfigExtension;

    public ViewConfigResolverProducer()
    {
    }

    public ViewConfigResolverProducer(ViewConfigExtension viewConfigExtension)
    {
        this.viewConfigExtension = viewConfigExtension;
    }

    @Produces
    @ApplicationScoped
    public ViewConfigResolver createViewConfigResolver()
    {
        if (!viewConfigExtension.isActivated())
        {
            return createEmptyDefaultViewConfigResolver();
        }

        if (!viewConfigExtension.isTransformed()) //esp. for easier unit-tests
        {
            viewConfigExtension.transformMetaDataTree();
        }
        ViewConfigResolver viewConfigResolver = viewConfigExtension.getViewConfigResolver();

        if (viewConfigResolver == null)
        {
            LOG.warning("It wasn't possible to create a ViewConfigResolver");
            viewConfigResolver = createEmptyDefaultViewConfigResolver();
        }

        return viewConfigResolver;
    }

    private DefaultViewConfigResolver createEmptyDefaultViewConfigResolver()
    {
        return new DefaultViewConfigResolver(
            new FolderConfigNode(
                null, null, new HashSet<Annotation>()), null, null, new ArrayList<ConfigDescriptorValidator>());
    }
}
