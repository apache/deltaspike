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

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.jsf.impl.token.PostRequestTokenManager;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import java.io.IOException;

@FacesRenderer(
    componentFamily = PostRequestTokenComponent.COMPONENT_FAMILY,
    rendererType = PostRequestTokenComponent.COMPONENT_TYPE)
public class RequestTokenHtmlRenderer extends Renderer
{
    private static final String INPUT_ELEMENT = "input";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String INPUT_TYPE_HIDDEN = "hidden";

    private static final String ID_ATTRIBUTE = "id";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String VALUE_ATTRIBUTE = "value";

    private volatile PostRequestTokenManager postRequestTokenManager;

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent component) throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(INPUT_ELEMENT, component);
        writer.writeAttribute(TYPE_ATTRIBUTE, INPUT_TYPE_HIDDEN, null);

        String clientId = component.getClientId(facesContext);
        writer.writeAttribute(ID_ATTRIBUTE, clientId, null);
        writer.writeAttribute(NAME_ATTRIBUTE, clientId, null);

        String currentPostRequestToken = getPostRequestTokenManager().getCurrentToken();
        if (currentPostRequestToken != null)
        {
            writer.writeAttribute(VALUE_ATTRIBUTE, currentPostRequestToken, VALUE_ATTRIBUTE);
        }

        writer.endElement(INPUT_ELEMENT);
    }

    //don't use #decode - we couldn't support DSP for immediate actions

    private PostRequestTokenManager getPostRequestTokenManager()
    {
        if (this.postRequestTokenManager == null)
        {
            synchronized (this)
            {
                if (this.postRequestTokenManager == null)
                {
                    this.postRequestTokenManager = BeanProvider.getContextualReference(PostRequestTokenManager.class);
                }
            }
        }

        return this.postRequestTokenManager;
    }
}
