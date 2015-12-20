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
package org.apache.deltaspike.test.jsf.impl.message.beans;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jsf.api.message.JsfMessage;

/**
 * sample backing bean for JsfMessage.
 */
@RequestScoped
@Named
public class JsfMessageBackingBean
{
    @Inject
    private JsfMessage<UserMessage> msg;

    private String locale = "en";


    public void init()
    {
        msg.addWarn().messageWithDetail("warnInfo");
        msg.addError().messageWithoutDetail("errorInfo");
        msg.addInfo().simpleMessageNoParam();
        msg.addFatal().simpleMessageWithParam("fatalInfo");
    }

    public String getSomeMessage()
    {
        return msg.get().simpleMessageNoParam();
    }

    public String getLocale()
    {
        return locale;
    }

    public void setLocale(String locale)
    {
        this.locale = locale;
    }
}
