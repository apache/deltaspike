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

import org.apache.deltaspike.core.api.config.view.controller.PreViewAction;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.jsf.impl.util.ViewControllerUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

/**
 * ActionListener which invokes {@link PreViewAction} callbacks of page-beans
 */
public class ViewControllerActionListener implements ActionListener, Deactivatable
{
    private final ActionListener wrapped;

    private final boolean activated;

    /**
     * Constructor for wrapping the given {@link ActionListener}
     * @param wrapped action-listener which should be wrapped
     */
    public ViewControllerActionListener(ActionListener wrapped)
    {
        this.wrapped = wrapped;
        this.activated = ClassDeactivationUtils.isActivated(getClass());
    }

    @Override
    public void processAction(ActionEvent actionEvent)
    {
        if (this.activated)
        {
            ViewConfigDescriptor viewConfigDescriptor = BeanProvider.getContextualReference(ViewConfigResolver.class)
                    .getViewConfigDescriptor(FacesContext.getCurrentInstance().getViewRoot().getViewId());

            ViewControllerUtils.executeViewControllerCallback(viewConfigDescriptor, PreViewAction.class);
        }

        this.wrapped.processAction(actionEvent);
    }
}
