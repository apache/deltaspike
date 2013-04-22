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
package org.apache.deltaspike.jsf.impl.message;

import javax.enterprise.inject.Typed;
import javax.faces.application.FacesMessage;
import java.io.Serializable;

/**
 * Entry for {@link FacesMessage} which have to be stored longer than a request
 */
@Typed()
public class FacesMessageEntry implements Serializable
{
    private static final long serialVersionUID = 6831499672107426470L;
    private String componentId;
    private FacesMessage facesMessage;

    protected FacesMessageEntry()
    {
    }

    /**
     * Constructor for creating the entry for the given component-id and {@link FacesMessage}
     * @param componentId current component-id
     * @param facesMessage current faces-message
     */
    public FacesMessageEntry(String componentId, FacesMessage facesMessage)
    {
        this.componentId = componentId;
        this.facesMessage = facesMessage;
    }

    /**
     * Returns the current component-id
     * @return component-id of the entry
     */
    public String getComponentId()
    {
        return componentId;
    }

    /**
     * Returns the current {@link FacesMessage}
     * @return faces-message of the entry
     */
    public FacesMessage getFacesMessage()
    {
        return facesMessage;
    }
}
