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
package org.apache.deltaspike.jsf.api.message;

import javax.faces.component.UIComponent;
import java.io.Serializable;

/**
 * <p>An injectable component for typesafe FacesMessages.
 * T must be a class which is annotated with
 * {@link org.apache.deltaspike.core.api.message.MessageBundle}</p>
 *
 * <p>Usage:
 * <pre>
 * &#064;Inject
 * JsfMessage&lt;MyMessages&gt; msg;
 * ...
 * msg.addError().userNotLoggedIn(user);
 * </pre>
 * <p>MessageBundle methods which are used as JsfMessage can return a
 * {@link org.apache.deltaspike.core.api.message.Message} or a String.
 * In case of a String we use it for both the summary and detail
 * information on the FacesMessage. </p>
 * <p>If a Message is returned, we lookup the 'detail' and 'summary'
 * categories (see {@link org.apache.deltaspike.core.api.message.Message#toString(String)}
 * for creating the FacesMessage.</p>
 *
 */
public interface JsfMessage<T> extends Serializable
{
    String CATEGORY_DETAIL = "detail";
    String CATEGORY_SUMMARY = "summary";

    /**
     * If the JsfMessage is used in a UIComponent we allow to set the clientId
     * @param clientId
     */
    JsfMessage<T> forClientId(String clientId);

    /**
     * @param uiComponent
     */
    JsfMessage<T> forComponent(UIComponent uiComponent);

    /**
     * @return the underlying Message which will automatically add a FacesMessage with SEVERITY_ERROR
     */
    T addError();

    /**
     * @return the underlying Message which will automatically add a FacesMessage with SEVERITY_FATAL
     */
    T addFatal();

    /**
     * @return the underlying Message which will automatically add a FacesMessage with SEVERITY_INFO
     */
    T addInfo();

    /**
     * @return the underlying Message which will automatically add a FacesMessage with SEVERITY_WARN
     */
    T addWarn();

    /**
     * @return the underlying Message implementation without adding any FacesMessage
     */
    T get();
}
