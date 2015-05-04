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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.destination.uc002;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.deltaspike.test.category.WebProfileCategory;
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


@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class ViewConfigTestDrone
{

    @Drone
    private WebDriver driver;

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive deploy()
    {
        return ShrinkWrap
                .create(WebArchive.class, "nav-destination-uc002.war")
                .addPackage(Pages.class.getPackage())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeSecurityArchive())
                .addAsWebResource("navigation/origin.xhtml", "/origin.xhtml")
                .addAsWebResource("navigation/pages/index.xhtml", "/pages/index.xhtml")
                .addAsWebResource("navigation/pages/home.xhtml", "/pages/home.xhtml")
                .addAsWebResource("navigation/pages/overview.xhtml", "/pages/overview.xhtml")
                .addAsWebResource("navigation/pages/customErrorPage.xhtml", "/pages/customErrorPage.xhtml")
                .addAsWebInfResource("default/WEB-INF/web.xml", "web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @RunAsClient
    public void testNavigationActionWithoutError() throws MalformedURLException
    {
        driver.get(new URL(contextPath, "origin.xhtml").toString());

        WebElement button = driver.findElement(By.id("destination:pb002ActionWithoutError"));
        button.click();
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(By.id("overviewPage"),
                "You arrived at overview page").apply(driver));
        // Was redirected ?
        Assert.assertTrue(driver.getCurrentUrl().contains("overview.xhtml"));
    }
    
    @Test
    @RunAsClient
    public void testNavigationActionWithError() throws MalformedURLException
    {
        driver.get(new URL(contextPath, "origin.xhtml").toString());

        WebElement button = driver.findElement(By.id("destination:pb002ActionWithError"));
        button.click();
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(By.id("customErrorPage"),
                "This is a custom error page").apply(driver));
    }


    @Test
    @RunAsClient
    public void testNavigationRestrictedToPages() throws MalformedURLException
    {
        driver.get(new URL(contextPath, "origin.xhtml").toString());

        WebElement button = driver.findElement(By.id("destination:pb002RestrictedToPages"));
        button.click();
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(By.id("homePage"), "You arrived at home page")
                .apply(driver));
        // Was fowarded ?
        Assert.assertTrue(driver.getCurrentUrl().contains("origin.xhtml"));
    }

}
