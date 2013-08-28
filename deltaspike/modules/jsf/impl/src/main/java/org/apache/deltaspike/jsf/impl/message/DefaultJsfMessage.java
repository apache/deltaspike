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
import javax.faces.component.UIComponent;

import java.lang.reflect.Proxy;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.impl.message.MessageBundleInvocationHandler;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.jsf.api.message.JsfMessage;

/**
 * Default implementation of JsfMessage.
 * The complexity of setting the FacesMessage is
 * done in the {@link JsfMessageBundleInvocationHandler}.
 */
@Typed
public class DefaultJsfMessage<T> implements JsfMessage<T>
{
    private final String clientId;
    private final Class<T> type;
    private final MessageBundleInvocationHandler invocationHandler;

    /**
     * The Message type
     * @param type
     * @param clientId
     */
    public DefaultJsfMessage(Class<T> type, String clientId, MessageBundleInvocationHandler invocationHandler)
    {
        this.type = type;
        this.clientId = clientId;
        this.invocationHandler = invocationHandler;

        if (! type.isInterface() || type.getAnnotation(MessageBundle.class) == null)
        {
            throw new IllegalArgumentException("JsfMessage must only be used for interfaces " +
                "annotated with @MessageBundle!");
        }
    }

    @Override
    public JsfMessage<T> forClientId(String clientId)
    {
        return new DefaultJsfMessage<T>(type, clientId, invocationHandler);
    }

    @Override
    public JsfMessage<T> forComponent(UIComponent uiComponent)
    {
        return forClientId(uiComponent.getClientId());
    }

    @Override
    public T addError()
    {
        return getMessage(FacesMessage.SEVERITY_ERROR);
    }

    @Override
    public T addFatal()
    {
        return getMessage(FacesMessage.SEVERITY_FATAL);
    }

    @Override
    public T addInfo()
    {
        return getMessage(FacesMessage.SEVERITY_INFO);
    }

    @Override
    public T addWarn()
    {
        return getMessage(FacesMessage.SEVERITY_WARN);
    }

    @Override
    public T get()
    {
        return getMessage(null);
    }

    private T getMessage(FacesMessage.Severity severity)
    {
        return type.cast(Proxy.newProxyInstance(ClassUtils.getClassLoader(null),
                new Class<?>[]{type}, new JsfMessageBundleInvocationHandler(severity, clientId, invocationHandler)));
    }

}
