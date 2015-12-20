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
package org.apache.deltaspike.test.jsf.impl.scope.mapped;


import java.net.URL;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.jsf.impl.config.TestJsfModuleConfig;
import org.apache.deltaspike.test.jsf.impl.scope.mapped.beans.JsfApplicationScopedBackingBean;
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
 * Test for the DeltaSpike ViewScoped context
 */
@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class MappedJsfContextTest
{
    @Drone
    private WebDriver driver;

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive deploy()
    {
        return ShrinkWrap
                .create(WebArchive.class, "mappedJsfContetxtTest.war")
                .addPackage(JsfApplicationScopedBackingBean.class.getPackage())
                .addClass(TestJsfModuleConfig.class)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeSecurityArchive())
                .addAsWebInfResource("default/WEB-INF/web.xml", "web.xml")
                .addAsWebResource("mappedJsfContextTest/page.xhtml", "page.xhtml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }


    @Test
    @RunAsClient
    public void testViewScopedContext() throws Exception
    {
        driver.get(new URL(contextPath, "page.xhtml?req=47&sess=48&app=49").toString());

        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(By.id("test:reqValue"), "47").apply(driver));
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(By.id("test:sessValue"), "48").apply(driver));
        Assert.assertTrue(ExpectedConditions.textToBePresentInElement(By.id("test:appValue"), "49").apply(driver));
    }

}
