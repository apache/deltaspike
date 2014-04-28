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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.parameter.uc002;

import java.net.URL;

import javax.inject.Inject;

import junit.framework.Assert;

import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.test.category.DeltaSpikeTest;
import org.apache.deltaspike.test.jsf.impl.config.view.navigation.parameter.shared.TestClassDeactivator;
import org.apache.deltaspike.test.jsf.impl.util.ArchiveUtils;
import org.apache.deltaspike.test.jsf.impl.util.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class StaticNavigationParameterWarFileTest
{
    @Deployment
    public static WebArchive deploy()
    {
        String simpleName = StaticNavigationParameterWarFileTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        URL fileUrl = StaticNavigationParameterWarFileTest.class.getClassLoader()
                .getResource("navigationParameterTest/apache-deltaspike.properties");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addClass(TestClassDeactivator.class)
                .addPackage(StaticNavigationParameterWarFileTest.class.getPackage())
                .addAsResource(FileUtils.getFileForURL(fileUrl.toString()), DeltaSpikeTest.DELTASPIKE_PROPERTIES)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeSecurityArchive())
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }

    @Inject
    private PageBean002 pageBean;

    @Inject
    private NavigationParameterContext navigationParameterContext;

    @Test
    public void oneParameter()
    {
        Assert.assertTrue(this.navigationParameterContext.getPageParameters().isEmpty());
        this.pageBean.actionMethod1();
        Assert.assertEquals(1, this.navigationParameterContext.getPageParameters().size());
        Assert.assertEquals("staticMarker", this.navigationParameterContext.getPageParameters().get("param1"));
    }

    @Test
    public void multipleParameters()
    {
        Assert.assertTrue(this.navigationParameterContext.getPageParameters().isEmpty());
        this.pageBean.actionMethod2();
        Assert.assertEquals(2, this.navigationParameterContext.getPageParameters().size());
        Assert.assertEquals("staticMarker1", this.navigationParameterContext.getPageParameters().get("param1"));
        Assert.assertEquals("staticMarker2", this.navigationParameterContext.getPageParameters().get("param2"));
    }
}
