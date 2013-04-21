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
package org.apache.deltaspike.jsf.impl.scope.window;

import javax.faces.context.FacesContext;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import java.util.Iterator;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;


/**
 * Registers the @{link WindowIdRenderKit}
 */
public class WindowIdRenderKitFactory extends RenderKitFactory implements Deactivatable
{
    private final RenderKitFactory wrapped;

    private final boolean deactivated;

    /**
     * Constructor for wrapping the given {@link javax.faces.render.RenderKitFactory}
     * @param wrapped render-kit-factory which will be wrapped
     */
    public WindowIdRenderKitFactory(RenderKitFactory wrapped)
    {
        this.wrapped = wrapped;
        this.deactivated = !isActivated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRenderKit(String s, RenderKit renderKit)
    {
        wrapped.addRenderKit(s, renderKit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RenderKit getRenderKit(FacesContext facesContext, String s)
    {
        RenderKit renderKit = wrapped.getRenderKit(facesContext, s);

        if (renderKit == null)
        {
            return null;
        }

        if (deactivated)
        {
            return renderKit;
        }

        return new WindowIdRenderKitWrapper(renderKit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> getRenderKitIds()
    {
        return wrapped.getRenderKitIds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RenderKitFactory getWrapped()
    {
        return wrapped;
    }

    public boolean isActivated()
    {
        return ClassDeactivationUtils.isActivated(getClass());
    }
}
