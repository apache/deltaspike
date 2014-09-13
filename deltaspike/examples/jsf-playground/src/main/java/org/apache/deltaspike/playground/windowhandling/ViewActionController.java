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
package org.apache.deltaspike.playground.windowhandling;

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;

@Named
@RequestScoped
public class ViewActionController
{
    @Inject
    private ClientWindow clientWindow;
    
    private Date lastTimeLinkAction;

    @PostConstruct
    public void init()
    {
        System.out.println("@PostConstruct ViewActionController");
    }

    public void action()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        System.out.println("ViewActionController#action with windowId: " + clientWindow.getWindowId(context));
    }

    public Date getLastTimeLinkAction()
    {
        return lastTimeLinkAction;
    }

    public void linkAction()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        System.out.println("ViewActionController#linkAction with windowId: " + clientWindow.getWindowId(context));
        lastTimeLinkAction = new Date();
    }
}
