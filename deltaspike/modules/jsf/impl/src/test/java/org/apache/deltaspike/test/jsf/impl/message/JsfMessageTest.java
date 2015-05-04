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
package org.apache.deltaspike.test.jsf.impl.message;


import java.net.URL;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.jsf.impl.config.TestJsfModuleConfig;
import org.apache.deltaspike.test.jsf.impl.message.beans.JsfMessageBackingBean;
import org.apache.deltaspike.test.jsf.impl.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;


/**
 * Test for the DeltaSpike JsfMessage Producer
 */
@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class JsfMessageTest
{
    @Drone
    private WebDriver driver;

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive deploy()
    {
        return ShrinkWrap
                .create(WebArchive.class, "jsfMessageTest.war")
                .addPackage(JsfMessageBackingBean.class.getPackage())
                .addClass(TestJsfModuleConfig.class)
                .addAsResource("jsfMessageTest/UserMessage_en.properties")
                .addAsResource("jsfMessageTest/UserMessage_de.properties")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeSecurityArchive())
                .addAsWebInfResource("default/WEB-INF/web.xml", "web.xml")
                .addAsWebResource("jsfMessageTest/page.xhtml", "page.xhtml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }


    @Test
    @RunAsClient
    public void testEnglishMessages() throws Exception
    {
        driver.get(new URL(contextPath, "page.xhtml").toString());

        //X comment this in if you like to debug the server
        //X I've already reported ARQGRA-213 for it
        //X System.out.println("contextpath= " + contextPath);
        //X Thread.sleep(600000L);

        // check the JSF FacesMessages
        Assert.assertNotNull(ExpectedConditions.presenceOfElementLocated(By.xpath("id('messages')")).apply(driver));

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.xpath("id('messages')/ul/li[1]"), "message with details warnInfo!").apply(driver));

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.xpath("id('messages')/ul/li[2]"), "message without detail but parameter errorInfo.").apply(driver));

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.xpath("id('messages')/ul/li[3]"), "a simple message without a param.").apply(driver));

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.xpath("id('messages')/ul/li[4]"), "simple message with a string param fatalInfo.").apply(driver));

        // check the free message usage
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.id("test:valueOutput"), "a simple message without a param.").apply(driver));

        // and also the usage via direct EL invocation
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.id("test:elOutput"), "a simple message without a param.").apply(driver));
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.id("test:elOutputWithParam"), "simple message with a string param hiho.").apply(driver));
    }

    @Test
    @RunAsClient
    public void testGermanMessages() throws Exception
    {
        driver.get(new URL(contextPath, "page.xhtml?lang=de").toString());

        // check the JSF FacesMessages
        Assert.assertNotNull(ExpectedConditions.presenceOfElementLocated(By.xpath("id('messages')")).apply(driver));

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.xpath("id('messages')/ul/li[1]"), "Nachricht mit Details warnInfo!").apply(driver));

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.xpath("id('messages')/ul/li[2]"), "Nachricht ohne Details aber mit Parameter errorInfo.").apply(driver));

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.xpath("id('messages')/ul/li[3]"), "Einfache Nachricht ohne Parameter.").apply(driver));

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.xpath("id('messages')/ul/li[4]"), "Einfache Nachricht mit String Parameter fatalInfo.").apply(driver));

        // check the free message usage
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                By.id("test:valueOutput"), "Einfache Nachricht ohne Parameter.").apply(driver));
    }

}
