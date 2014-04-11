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
package org.apache.deltaspike.jsf.impl.token;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.jsf.api.listener.phase.JsfPhaseListener;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;
import java.util.Map;

//ignore jsf-ajax requests since they have to be queued according to the spec.
//ignore get-requests since they >shouldn't< change the state (we couldn't support them at all)
//post-requests don't get pipelined -> no need to sync. them per session
//browser-window-handling is done implicitly (PostRequestTokenManager is window-scoped)
@JsfPhaseListener(ordinal = 9000)
public class DoubleSubmitAwarePhaseListener implements PhaseListener, Deactivatable
{
    private static final long serialVersionUID = -4247051429332418226L;

    @Inject
    private PostRequestTokenManager postRequestTokenManager;

    @Override
    public void afterPhase(PhaseEvent event)
    {
        FacesContext facesContext = event.getFacesContext();

        //only check full POST requests
        if (facesContext.isPostback() && !facesContext.getPartialViewContext().isAjaxRequest())
        {
            String receivedPostRequestToken = facesContext.getExternalContext()
                .getRequestParameterMap().get(PostRequestTokenMarker.POST_REQUEST_TOKEN_KEY);

            if (receivedPostRequestToken == null)
            {
                receivedPostRequestToken = findPostRequestTokenWithPrefix(facesContext);
            }

            if (!this.postRequestTokenManager.isValidRequest(receivedPostRequestToken))
            {
                facesContext.renderResponse();
            }
        }
    }

    @Override
    public void beforePhase(PhaseEvent event)
    {
        //refresh the token in case of GET-requests to avoid that the token is re-used on the next page
        if (!event.getFacesContext().isPostback())
        {
            this.postRequestTokenManager.createNewToken();
        }
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.RESTORE_VIEW;
    }

    protected String findPostRequestTokenWithPrefix(FacesContext facesContext)
    {
        for (Map.Entry<String, String> parameterEntry :
            facesContext.getExternalContext().getRequestParameterMap().entrySet())
        {
            if (parameterEntry.getKey().endsWith(PostRequestTokenMarker.POST_REQUEST_TOKEN_WITH_PREFIX_KEY) ||
                parameterEntry.getKey().endsWith(PostRequestTokenMarker.POST_REQUEST_TOKEN_WITH_MANUAL_PREFIX_KEY))
            {
                return parameterEntry.getValue();
            }
        }
        return null;
    }
}
