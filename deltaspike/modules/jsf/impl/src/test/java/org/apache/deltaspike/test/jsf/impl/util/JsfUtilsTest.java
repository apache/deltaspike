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
package org.apache.deltaspike.test.jsf.impl.util;

import org.apache.deltaspike.jsf.impl.util.JsfUtils;
import org.junit.Assert;
import org.junit.Test;

import jakarta.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

//several tests are only needed to check that there won't be a NullPointerException
public class JsfUtilsTest
{
    @Test
    public void testNewMessageWithoutExistingMessages()
    {
        List<FacesMessage> existingFacesMessage = new ArrayList<FacesMessage>();
        Assert.assertEquals(Boolean.TRUE,
            JsfUtils.isNewMessage(existingFacesMessage, new FacesMessage("test")));
    }

    @Test
    public void testExistingSimpleMessage()
    {
        List<FacesMessage> existingFacesMessage = new ArrayList<FacesMessage>();
        existingFacesMessage.add(new FacesMessage("test"));
        FacesMessage messageToCheck = new FacesMessage("test");
        Assert.assertEquals(Boolean.FALSE,
            JsfUtils.isNewMessage(existingFacesMessage, messageToCheck));
    }

    @Test
    public void testExistingMessageWithoutSummary()
    {
        List<FacesMessage> existingFacesMessage = new ArrayList<FacesMessage>();
        existingFacesMessage.add(new FacesMessage(null, "test"));
        FacesMessage messageToCheck = new FacesMessage(null, "test");
        Assert.assertEquals(Boolean.FALSE,
            JsfUtils.isNewMessage(existingFacesMessage, messageToCheck));
    }

    @Test
    public void testExistingMessageWithoutDetails()
    {
        List<FacesMessage> existingFacesMessage = new ArrayList<FacesMessage>();
        existingFacesMessage.add(new FacesMessage("test", null));
        FacesMessage messageToCheck = new FacesMessage("test", null);
        Assert.assertEquals(Boolean.FALSE,
            JsfUtils.isNewMessage(existingFacesMessage, messageToCheck));
    }

    @Test
    public void testNewMessageWithExistingMessageWithoutText()
    {
        List<FacesMessage> existingFacesMessage = new ArrayList<FacesMessage>();
        existingFacesMessage.add(new FacesMessage(null, null));
        FacesMessage messageToCheck = new FacesMessage("test");
        Assert.assertEquals(Boolean.TRUE,
            JsfUtils.isNewMessage(existingFacesMessage, messageToCheck));
    }

    @Test
    public void testNewMessageWithoutText()
    {
        List<FacesMessage> existingFacesMessage = new ArrayList<FacesMessage>();
        existingFacesMessage.add(new FacesMessage("existing", "message"));
        FacesMessage messageToCheck = new FacesMessage(null, null);
        Assert.assertEquals(Boolean.TRUE,
            JsfUtils.isNewMessage(existingFacesMessage, messageToCheck));
    }

    @Test
    public void testNewMessageWithoutSummary()
    {
        List<FacesMessage> existingFacesMessage = new ArrayList<FacesMessage>();
        existingFacesMessage.add(new FacesMessage("existing", "message"));
        FacesMessage messageToCheck = new FacesMessage(null, "test");
        Assert.assertEquals(Boolean.TRUE,
            JsfUtils.isNewMessage(existingFacesMessage, messageToCheck));
    }

    @Test
    public void testNewMessageWithoutDetails()
    {
        List<FacesMessage> existingFacesMessage = new ArrayList<FacesMessage>();
        existingFacesMessage.add(new FacesMessage("existing", "message"));
        FacesMessage messageToCheck = new FacesMessage("test", null);
        Assert.assertEquals(Boolean.TRUE,
            JsfUtils.isNewMessage(existingFacesMessage, messageToCheck));
    }

    @Test
    public void testMessagesWithoutText()
    {
        List<FacesMessage> existingFacesMessage = new ArrayList<FacesMessage>();
        existingFacesMessage.add(new FacesMessage(null, null));
        FacesMessage messageToCheck = new FacesMessage(null, null);
        Assert.assertEquals(Boolean.FALSE,
            JsfUtils.isNewMessage(existingFacesMessage, messageToCheck));
    }

    @Test
    public void testNewMessageWithExistingMessageWithoutSummary()
    {
        List<FacesMessage> existingFacesMessage = new ArrayList<FacesMessage>();
        existingFacesMessage.add(new FacesMessage(null, "message"));
        FacesMessage messageToCheck = new FacesMessage("new", "message");
        Assert.assertEquals(Boolean.TRUE,
            JsfUtils.isNewMessage(existingFacesMessage, messageToCheck));
    }

    @Test
    public void testNewMessageWithExistingMessageWithoutDetails()
    {
        List<FacesMessage> existingFacesMessage = new ArrayList<FacesMessage>();
        existingFacesMessage.add(new FacesMessage("existing", null));
        FacesMessage messageToCheck = new FacesMessage("new", "message");
        Assert.assertEquals(Boolean.TRUE,
            JsfUtils.isNewMessage(existingFacesMessage, messageToCheck));
    }
}
