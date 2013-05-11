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
import javax.faces.context.FacesContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.deltaspike.core.api.message.Message;
import org.apache.deltaspike.core.impl.message.MessageBundleInvocationHandler;
import org.apache.deltaspike.jsf.api.message.JsfMessage;

/**
 * This Proxy InvocationHandler automatically registers the
 * returned messages in the FacesContext if a severity is set.
 */
@Typed()
public class JsfMessageBundleInvocationHandler implements InvocationHandler
{
    private final FacesMessage.Severity severity;
    private final String clientId;
    private final MessageBundleInvocationHandler invocationHandler;

    public JsfMessageBundleInvocationHandler(FacesMessage.Severity severity, String clientId,
                                             MessageBundleInvocationHandler invocationHandler)
    {
        this.severity = severity;
        this.clientId = clientId;
        this.invocationHandler = invocationHandler;
    }

    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
    {
        Object message = invocationHandler.invoke(proxy, method, args);

        if (severity == null)
        {
            if (message instanceof Message)
            {
                return message;
            }

            return getMessageCategory(message, JsfMessage.CATEGORY_SUMMARY);
        }
        else
        {
            String summary = getMessageCategory(message, JsfMessage.CATEGORY_SUMMARY);
            String detail = getMessageCategory(message, JsfMessage.CATEGORY_DETAIL);

            FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(severity, summary, detail));

            return message;
        }
    }

    private String getMessageCategory(Object message, String category)
    {
        if (message == null)
        {
            return null;
        }

        if (message instanceof String)
        {
            return (String) message;
        }
        else if (message instanceof Message)
        {
            return ((Message) message).toString(category);
        }
        else
        {
            throw new IllegalArgumentException("message must be of either type String or Message but was: " +
                message.getClass() + " value: " + message);
        }
    }

}
