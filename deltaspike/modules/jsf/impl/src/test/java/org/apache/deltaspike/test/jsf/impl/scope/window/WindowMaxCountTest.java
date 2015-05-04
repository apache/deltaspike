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

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class WindowMaxCountTest
{

    @ArquillianResource
    private URL contextPath;

    @Drone
    private WebDriver driver;

    @Deployment
    public static WebArchive deploy()
    {
        WebArchive archive = ShrinkWrap
                .create(WebArchive.class, "windowMaxCountTest.war")
                .addPackage(WindowMaxCountTest.class.getPackage())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeSecurityArchive())
                .addAsWebResource("windowScopedContextTest/windowcount.xhtml", "/windowcount.xhtml")
                .addAsWebInfResource("default/WEB-INF/web.xml", "web.xml")
                .addAsWebInfResource("META-INF/apache-deltaspike.properties",
                        "classes/META-INF/apache-deltaspike.properties")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @Test
    @RunAsClient
    public void maxWindowPerSessionTest() throws MalformedURLException
    {
        // PAGE 1 - REQUEST 1
        driver.get(new URL(contextPath, "windowcount.xhtml").toString());
        // click once
        WebElement button = driver.findElement(By.id("form:count"));
        button.click();
        WebElement value = driver.findElement(By.id("form:value"));
        Assert.assertEquals("1", value.getText());
        driver.get(driver.getCurrentUrl());
        // click twice
        button = driver.findElement(By.id("form:count"));
        button.click();
        value = driver.findElement(By.id("form:value"));
        Assert.assertEquals("2", value.getText());
        String page1 = driver.getCurrentUrl();

        // PAGE 2 - REQUEST 2
        driver.get(new URL(contextPath, "windowcount.xhtml").toString());
        // click once
        button = driver.findElement(By.id("form:count"));
        button.click();
        value = driver.findElement(By.id("form:value"));
        Assert.assertEquals("1", value.getText());
        driver.get(driver.getCurrentUrl());
        // click twice
        button = driver.findElement(By.id("form:count"));
        button.click();
        value = driver.findElement(By.id("form:value"));
        Assert.assertEquals("2", value.getText());

        // PAGE 3 - REQUEST 3 to force the oldest (page 1) WindowScope to drop
        driver.get(new URL(contextPath, "windowcount.xhtml").toString());
        // click once
        button = driver.findElement(By.id("form:count"));
        button.click();
        value = driver.findElement(By.id("form:value"));
        Assert.assertEquals("1", value.getText());
        driver.get(driver.getCurrentUrl());
        // click twice
        button = driver.findElement(By.id("form:count"));
        button.click();
        value = driver.findElement(By.id("form:value"));
        Assert.assertEquals("2", value.getText());
        String page3 = driver.getCurrentUrl();

        // PAGE 1 - REQUEST 4 on previous PAGE 1 page (value should get dropped to 0)
        driver.get(new URL(page1).toString());
        // click once
        button = driver.findElement(By.id("form:count"));
        button.click();
        value = driver.findElement(By.id("form:value"));
        // Value should return to 1
        Assert.assertEquals("1", value.getText());

        // PAGE 3 - REQUEST 5 on previous PAGE 3 page (page 1 and page 3 should be the two active pages)
        driver.get(new URL(page3).toString());
        button = driver.findElement(By.id("form:count"));
        // click once
        button.click();
        value = driver.findElement(By.id("form:value"));
        // Value should continue to 3
        Assert.assertEquals("3", value.getText());
    }

}
