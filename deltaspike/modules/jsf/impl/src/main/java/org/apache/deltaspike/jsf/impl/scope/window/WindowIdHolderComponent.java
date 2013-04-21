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

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * UI Component holder for the windowId in case of post-backs.
 * We store this component as direct child in the ViewRoot
 * and evaluate it's value on postbacks.
 */
public class WindowIdHolderComponent extends UIOutput
{
    private static final Logger logger = Logger.getLogger(WindowIdHolderComponent.class.getName());

    private String windowId;

    /**
     * Default constructor might be invoked by the jsf implementation
     */
    @SuppressWarnings("UnusedDeclaration")
    public WindowIdHolderComponent()
    {
    }

    /**
     * Constructor which creates the holder for the given window-id
     * @param windowId current window-id
     */
    public WindowIdHolderComponent(String windowId)
    {
        this.windowId = windowId;
    }

    /**
     * Needed for server-side window-handler and client-side window handler for supporting postbacks
     */
    public static void addWindowIdHolderComponent(FacesContext facesContext, String windowId)
    {
        if (windowId == null || windowId.length() == 0)
        {
            return;
        }

        UIViewRoot uiViewRoot = facesContext.getViewRoot();

        if (uiViewRoot == null)
        {
            return;
        }

        WindowIdHolderComponent existingWindowIdHolder = getWindowIdHolderComponent(uiViewRoot);
        if (existingWindowIdHolder != null)
        {
            if (!windowId.equals(existingWindowIdHolder.getWindowId()))
            {
                logger.log(Level.FINE, "updating WindowIdHolderComponent from %1 to %2",
                        new Object[]{existingWindowIdHolder.getId(), windowId});

                existingWindowIdHolder.changeWindowId(windowId);
            }
            return;
        }
        else
        {
            // add as first child
            uiViewRoot.getChildren().add(0, new WindowIdHolderComponent(windowId));
        }
    }

    public static WindowIdHolderComponent getWindowIdHolderComponent(UIViewRoot uiViewRoot)
    {
        List<UIComponent> uiComponents = uiViewRoot.getChildren();

        // performance improvement - don't change - see EXTCDI-256 :
        for (int i = 0, size = uiComponents.size(); i < size; i++)
        {
            UIComponent uiComponent = uiComponents.get(i);
            if (uiComponent instanceof WindowIdHolderComponent)
            {
                //in this case we have the same view-root
                return (WindowIdHolderComponent) uiComponent;
            }
        }

        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object saveState(FacesContext facesContext)
    {
        Object[] values = new Object[2];
        values[0] = super.saveState(facesContext);
        values[1] = windowId;
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreState(FacesContext facesContext, Object state)
    {
        if (state == null)
        {
            return;
        }

        Object[] values = (Object[]) state;
        super.restoreState(facesContext, values[0]);

        windowId = (String) values[1];
    }

    /**
     * @return the current windowId
     */
    public String getWindowId()
    {
        return windowId;
    }

    void changeWindowId(String windowId)
    {
        this.windowId = windowId;
    }
}
