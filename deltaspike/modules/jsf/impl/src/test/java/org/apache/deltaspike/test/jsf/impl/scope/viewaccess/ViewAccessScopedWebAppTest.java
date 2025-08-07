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

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.control.LockedImplementation;
import org.apache.deltaspike.test.control.VersionControlRule;
import org.apache.deltaspike.test.jsf.impl.scope.viewaccess.beans.ViewAccessScopedBeanX;
import org.apache.deltaspike.test.jsf.impl.scope.viewaccess.beans.ViewAccessScopedBeanY;
import org.apache.deltaspike.test.jsf.impl.util.ArchiveUtils;
import org.apache.deltaspike.test.utils.Implementation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.URL;

import static org.apache.deltaspike.test.utils.BeansXmlUtil.BEANS_XML_ALL;

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class ViewAccessScopedWebAppTest
{
    @ArquillianResource
    private URL contextPath;

    @Rule
    public VersionControlRule versionControlRule = new VersionControlRule();

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
                .addAsWebInfResource(BEANS_XML_ALL, "beans.xml");
    }

    @Test
    @RunAsClient
    // Actually not a MyFaces bug but html-unit uses Rhino which cannot handle EcmaScript spread operator syntax yet
    // See https://github.com/mozilla/rhino/issues/968
    @LockedImplementation(excludedImplementations = {Implementation.MYFACES40})
    public void testForward() throws Exception
    {
        WebDriver driver = new HtmlUnitDriver(true);
        driver.get(new URL(contextPath, "page1.xhtml").toString());

        WebElement inputFieldX = driver.findElement(By.id("testForm1:valueInputX"));
        inputFieldX.sendKeys("abc");
        WebElement inputFieldY = driver.findElement(By.id("testForm1:valueInputY"));
        inputFieldY.sendKeys("xyz");

        WebElement button = driver.findElement(By.id("testForm1:next"));
        button.click();

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                driver.findElement(By.id("valueX")), "abc").apply(driver));

        button = driver.findElement(By.id("testForm2:back"));
        button.click();

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(
                driver.findElement(By.id("valueOutputX")), "abc").apply(driver));
        Assert.assertFalse(ExpectedConditions.textToBePresentInElement(
                driver.findElement( By.id("valueOutputY")), "xyz").apply(driver));
    }
}