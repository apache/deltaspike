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
package org.apache.deltaspike.test.jsf.impl.injection.uc003;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.jsf.impl.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.MalformedURLException;
import java.net.URL;

import static org.apache.deltaspike.test.utils.BeansXmlUtil.BEANS_XML_ALL;

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class InjectionDroneTest
{

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive deploy()
    {
        return ShrinkWrap
                .create(WebArchive.class, "injection-uc003.war")
                .addPackage(MyBean.class.getPackage())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeSecurityArchive())
                .addAsWebResource("injection/testValidatorConverterTag.xhtml", "/testValidatorConverter.xhtml")
                .addAsWebInfResource("META-INF/test.taglib.xml", "classes/META-INF/test.taglib.xml")
                .addAsWebInfResource("default/WEB-INF/web.xml", "web.xml")
                .addAsWebInfResource("default/WEB-INF/faces-config.xml", "faces-config.xml")
                .addAsWebInfResource(BEANS_XML_ALL, "beans.xml");
    }

    @Test
    @RunAsClient
    public void testConverter() throws MalformedURLException
    {
        WebDriver driver = new HtmlUnitDriver(true);
        driver.get(new URL(contextPath, "testValidatorConverter.xhtml").toString());
        WebElement convertedValue = driver.findElement(By.id("converter:convertedValue"));
        convertedValue.sendKeys("123");
        WebElement testConverterButton = driver.findElement(By.id("converter:testConverterButton"));
        testConverterButton.click();
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                driver.findElement(By.id("messages")), "Worked").apply(driver));
    }

    @Test
    @RunAsClient
    public void testConverterWithError() throws MalformedURLException
    {
        WebDriver driver = new HtmlUnitDriver(true);
        driver.get(new URL(contextPath, "testValidatorConverter.xhtml").toString());
        WebElement convertedValue = driver.findElement(By.id("converter:convertedValue"));
        convertedValue.sendKeys("String Value");
        WebElement testConverterButton = driver.findElement(By.id("converter:testConverterButton"));
        testConverterButton.click();
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                driver.findElement(By.id("converter:errorMessage")),
                "Value is not an Integer").apply(driver));
    }

    @Test
    @RunAsClient
    public void testValidator() throws MalformedURLException
    {
        WebDriver driver = new HtmlUnitDriver(true);
        driver.get(new URL(contextPath, "testValidatorConverter.xhtml").toString());
        WebElement convertedValue = driver.findElement(By.id("validator:stringValue"));
        convertedValue.sendKeys("DeltaSpike");
        WebElement testConverterButton = driver.findElement(By.id("validator:testValidatorButton"));
        testConverterButton.click();
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                driver.findElement(By.id("messages")), "Worked").apply(driver));
    }

    @Test
    @RunAsClient
    public void testValidatorWithError() throws MalformedURLException
    {
        WebDriver driver = new HtmlUnitDriver(true);
        driver.get(new URL(contextPath, "testValidatorConverter.xhtml").toString());
        WebElement convertedValue = driver.findElement(By.id("validator:stringValue"));
        convertedValue.sendKeys("Wrong Value");
        WebElement testConverterButton = driver.findElement(By.id("validator:testValidatorButton"));
        testConverterButton.click();
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                driver.findElement(By.id("validator:errorMessage")),
                "Not a valid value").apply(driver));
    }
}
