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
package org.apache.deltaspike.test.jsf.impl.scope.window;


import java.net.URL;
import java.util.logging.Logger;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.jsf.impl.config.TestJsfModuleConfig;
import org.apache.deltaspike.test.jsf.impl.scope.window.beans.WindowScopedBackingBean;
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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;


/**
 * Test for the DeltaSpike JsfMessage Producer
 */
@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class WindowScopedContextTest
{
    private static final Logger log = Logger.getLogger(WindowScopedContextTest.class.getName());

    @Drone
    private WebDriver driver;

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive deploy()
    {
        return ShrinkWrap
                .create(WebArchive.class, "windowScopedContextTest.war")
                .addPackage(WindowScopedBackingBean.class.getPackage())
                .addClass(TestJsfModuleConfig.class)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeSecurityArchive())
                .addAsWebInfResource("default/WEB-INF/web.xml", "web.xml")
                .addAsWebResource("META-INF/resources/deltaspike/windowhandler.js",
                                  "resources/deltaspike/windowhandler.js")
                .addAsWebResource("windowScopedContextTest/page.xhtml", "page.xhtml")
                .addAsWebResource("windowScopedContextTest/page2.xhtml", "page2.xhtml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }


    @Test
    @RunAsClient
    public void testWindowId() throws Exception
    {
        System.out.println("contextpath= " + contextPath);

        //X comment this in if you like to debug the server
        //X I've already reported ARQGRA-213 for it
        //X Thread.sleep(600000L);

        driver.get(new URL(contextPath, "page.xhtml").toString());

        WebElement inputField = driver.findElement(By.id("test:valueInput"));
        inputField.sendKeys("23");

        WebElement button = driver.findElement(By.id("test:saveButton"));
        button.click();

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(By.id("valueOutput"), "23").apply(driver));

    }


}
