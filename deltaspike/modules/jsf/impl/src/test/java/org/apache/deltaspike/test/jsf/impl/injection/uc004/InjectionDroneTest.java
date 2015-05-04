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
package org.apache.deltaspike.test.jsf.impl.injection.uc004;

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
public class InjectionDroneTest
{
    @Drone
    private WebDriver driver;

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive deploy()
    {
        return ShrinkWrap
                .create(WebArchive.class, "injection-uc004.war")
                .addPackage(MyBean.class.getPackage())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeSecurityArchive())
                .addAsWebResource("injection/testValidatorTagParameter.xhtml", "/testValidatorConverter.xhtml")
                .addAsWebInfResource("META-INF/test.taglib.xml", "classes/META-INF/test.taglib.xml")
                .addAsWebInfResource("default/WEB-INF/web.xml", "web.xml")
                .addAsWebInfResource("default/WEB-INF/faces-config.xml", "faces-config.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @RunAsClient
    public void testValidator() throws MalformedURLException
    {
        driver.get(new URL(contextPath, "testValidatorConverter.xhtml").toString());
        WebElement convertedValue = driver.findElement(By.id("validator:stringValue"));
        convertedValue.sendKeys("DeltaSpike");
        WebElement testConveterButton = driver.findElement(By.id("validator:testValidatorButton"));
        testConveterButton.click();
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(By.id("messages"), "Worked").apply(driver));
    }

    @Test
    @RunAsClient
    public void testValidatorWithError() throws MalformedURLException
    {
        driver.get(new URL(contextPath, "testValidatorConverter.xhtml").toString());
        WebElement convertedValue = driver.findElement(By.id("validator:stringValue"));
        convertedValue.sendKeys("Wrong Value");
        WebElement testConveterButton = driver.findElement(By.id("validator:testValidatorButton"));
        testConveterButton.click();
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(By.id("validator:errorMessage"),
                "The valid value should be DeltaSpike").apply(driver));
    }
}
