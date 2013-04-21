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
package org.apache.deltaspike.jsf.impl.listener.action;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.jsf.impl.config.view.ViewControllerActionListener;

import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

/**
 * Aggregates {@link ActionListener} implementations provided by DeltaSpike to ensure a deterministic behaviour
 */
public class DeltaSpikeActionListener implements ActionListener, Deactivatable
{
    private final ActionListener wrapped;
    private final boolean activated;

    /**
     * Constructor for wrapping the given {@link ActionListener}
     * @param wrapped action-listener which should be wrapped
     */
    public DeltaSpikeActionListener(ActionListener wrapped)
    {
        this.wrapped = wrapped;
        this.activated = ClassDeactivationUtils.isActivated(getClass());
    }

    @Override
    public void processAction(ActionEvent actionEvent)
    {
        if (this.activated)
        {
            getWrappedActionListener().processAction(actionEvent);
        }
        else
        {
            this.wrapped.processAction(actionEvent);
        }
    }

    private ActionListener getWrappedActionListener()
    {
        //TODO re-visit it
        //was:
        //SecurityViolationAwareActionListener securityViolationAwareActionListener =
        //        new SecurityViolationAwareActionListener(this.wrapped);

        return new ViewControllerActionListener(this.wrapped);
    }
}
