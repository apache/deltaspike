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
package org.apache.deltaspike.jsf.impl.listener.request;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;

public class DeltaSpikeFacesContextFactory extends FacesContextFactory implements Deactivatable
{
    private final FacesContextFactory wrappedFacesContextFactory;

    private final boolean deactivated;

    /**
     * Constructor for wrapping the given {@link FacesContextFactory}
     *
     * @param wrappedFacesContextFactory wrapped faces-context-factory which should be used
     */
    public DeltaSpikeFacesContextFactory(FacesContextFactory wrappedFacesContextFactory)
    {
        this.wrappedFacesContextFactory = wrappedFacesContextFactory;
        this.deactivated = !ClassDeactivationUtils.isActivated(getClass());
    }

    /**
     * Wrapps the created {@link jakarta.faces.context.FacesContext} with {@link DeltaSpikeFacesContextWrapper}
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public FacesContext getFacesContext(Object context,
                                        Object request,
                                        Object response,
                                        Lifecycle lifecycle)
    {
        FacesContext facesContext =
                this.wrappedFacesContextFactory.getFacesContext(context, request, response, lifecycle);

        if (facesContext == null || this.deactivated || facesContext instanceof DeltaSpikeFacesContextWrapper)
        {
            return facesContext;
        }
        
        return new DeltaSpikeFacesContextWrapper(facesContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FacesContextFactory getWrapped()
    {
        return wrappedFacesContextFactory;
    }
}