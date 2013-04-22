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
package org.apache.deltaspike.jsf.impl.listener.phase;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.deltaspike.jsf.impl.message.FacesMessageEntry;

import java.io.Serializable;
import java.util.List;

@WindowScoped
public class WindowMetaData implements Serializable
{
    private static final long serialVersionUID = -413165700186583037L;

    private String initializedViewId;

    /**
     * used per default instead of Flash#setKeepMessages,
     * because there are less issues in view of multi-window support esp. before jsf v2.2 and
     * a custom window-handler might have special requirements.
     */
    private List<FacesMessageEntry> facesMessageEntryList;

    public String getInitializedViewId()
    {
        return initializedViewId;
    }

    public void setInitializedViewId(String initializedViewId)
    {
        this.initializedViewId = initializedViewId;
    }

    public void setFacesMessageEntryList(List<FacesMessageEntry> facesMessageEntryList)
    {
        this.facesMessageEntryList = facesMessageEntryList;
    }

    public List<FacesMessageEntry> getFacesMessageEntryList()
    {
        return facesMessageEntryList;
    }
}
