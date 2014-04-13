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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.parameter.uc004;

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

import javax.inject.Inject;
import java.net.URL;


@RunWith(Arquillian.class)
public class DynamicNavigationParameterWarFileTest
{
    @Deployment
    public static WebArchive deploy()
    {
        String simpleName = DynamicNavigationParameterWarFileTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        URL fileUrl = DynamicNavigationParameterWarFileTest.class.getClassLoader()
                .getResource("navigationParameterTest/apache-deltaspike.properties");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addClass(TestClassDeactivator.class)
                .addPackage(DynamicNavigationParameterWarFileTest.class.getPackage())
                .addAsResource(FileUtils.getFileForURL(fileUrl.toString()), DeltaSpikeTest.DELTASPIKE_PROPERTIES)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeSecurityArchive())
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }

    @Inject
    private PageBean004 pageBean;

    @Inject
    private NavigationParameterContext navigationParameterContext;

    @Test
    public void dynamicParameters()
    {
        Assert.assertTrue(this.navigationParameterContext.getPageParameters().isEmpty());

        this.pageBean.actionMethod();
        Assert.assertTrue(this.navigationParameterContext.getPageParameters().isEmpty());

        this.pageBean.actionMethod();
        Assert.assertEquals(1, this.navigationParameterContext.getPageParameters().size());
        Assert.assertEquals("0", this.navigationParameterContext.getPageParameters().get("cv"));

        this.pageBean.actionMethod();
        Assert.assertEquals(1, this.navigationParameterContext.getPageParameters().size());
        Assert.assertEquals("1", this.navigationParameterContext.getPageParameters().get("cv"));
    }
}
