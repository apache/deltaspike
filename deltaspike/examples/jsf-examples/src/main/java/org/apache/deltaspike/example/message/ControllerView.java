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

import org.apache.deltaspike.jsf.api.message.JsfMessage;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 *
 */
@Named
@RequestScoped
public class ControllerView
{

    private String name;

    @Inject
    private JsfMessage<ApplicationMessages> msg;

    @Inject
    private JsfMessage<CustomizedMessages> custom;

    public void doGreeting()
    {
        msg.addInfo().helloWorld(name);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String someName)
    {
        name = someName;
    }

    public String getNow()
    {
        // getTimestampMessage return a Message where you could do some customizations before calling toString().
        return custom.get().getTimestampMessage(new Date()).toString();
    }

    public String getCustomMessage()
    {
        return custom.get().fromFacesMessageBundle();
    }
}
