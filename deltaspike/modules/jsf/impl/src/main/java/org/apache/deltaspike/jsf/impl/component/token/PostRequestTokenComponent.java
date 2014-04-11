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
package org.apache.deltaspike.jsf.impl.component.token;

import org.apache.deltaspike.jsf.impl.token.PostRequestTokenMarker;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;


/**
 * Component for rendering the post-request-token
 */
@FacesComponent(PostRequestTokenComponent.COMPONENT_TYPE)
public class PostRequestTokenComponent extends UIInput
{
    public static final String COMPONENT_TYPE = "org.apache.deltaspike.PostRequestTokenHolder";

    private transient String markedId;

    @Override
    public String getId()
    {
        if (this.markedId == null)
        {
            String originalId = super.getId();

            if (originalId.contains(PostRequestTokenMarker.POST_REQUEST_TOKEN_KEY))
            {
                this.markedId = originalId;
            }
            else
            {
                this.markedId = originalId + "_" + PostRequestTokenMarker.POST_REQUEST_TOKEN_KEY;
            }
        }
        return this.markedId;
    }

    //don't use #restoreState - we couldn't support stateless views,...
}
