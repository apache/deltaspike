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
package org.apache.deltaspike.example.message;

import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.jsf.impl.message.JsfMessageResolver;

import javax.enterprise.inject.Specializes;
import javax.faces.context.FacesContext;

@Specializes
public class CustomMessageResolver extends JsfMessageResolver
{
    private static final long serialVersionUID = -7566133260553818285L;

    @Override
    public String getMessage(MessageContext messageContext, String messageTemplate, String category)
    {
        addMessageBundleFromFacesConfig(messageContext);
        return super.getMessage(messageContext, messageTemplate, category);
    }

    private void addMessageBundleFromFacesConfig(MessageContext someMessageContext)
    {
        String messageBundle = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
        if (messageBundle != null && messageBundle.length() > 0)
        {
            someMessageContext.messageSource(messageBundle);
        }
    }

}
