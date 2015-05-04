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
package org.apache.deltaspike.test.jsf.impl.scope.viewaccess;

import java.net.URL;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.jsf.impl.scope.viewaccess.beans.ViewAccessScopedBeanX;
import org.apache.deltaspike.test.jsf.impl.scope.viewaccess.beans.ViewAccessScopedBeanY;
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
public class ViewAccessScopedWebAppTest
{
    @Drone
    private WebDriver driver;

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive deploy()
    {
        String simpleName = ViewAccessScopedWebAppTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        return ShrinkWrap
                .create(WebArchive.class, archiveName + ".war")
                .addClass(ViewAccessScopedBeanX.class)
                .addClass(ViewAccessScopedBeanY.class)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeSecurityArchive())
                .addAsWebInfResource("default/WEB-INF/web.xml", "web.xml")
                .addAsWebResource("viewAccessScopedContextTest/page1.xhtml", "page1.xhtml")
                .addAsWebResource("viewAccessScopedContextTest/page2.xhtml", "page2.xhtml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @RunAsClient
    public void testForward() throws Exception
    {
        driver.get(new URL(contextPath, "page1.xhtml").toString());

        WebElement inputFieldX = driver.findElement(By.id("testForm1:valueInputX"));
        inputFieldX.sendKeys("abc");
        WebElement inputFieldY = driver.findElement(By.id("testForm1:valueInputY"));
        inputFieldY.sendKeys("xyz");

        WebElement button = driver.findElement(By.id("testForm1:next"));
        button.click();

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(By.id("valueX"), "abc").apply(driver));

        button = driver.findElement(By.id("testForm2:back"));
        button.click();

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(By.id("valueOutputX"), "abc").apply(driver));
        Assert.assertFalse(ExpectedConditions.textToBePresentInElement(By.id("valueOutputY"), "xyz").apply(driver));
    }
}