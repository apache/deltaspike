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
package org.apache.deltaspike.integration.core.api.exclude;


import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.impl.projectstage.ProjectStageProducer;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.integration.core.api.projectstage.IntegrationTestProjectStageProducer;
import org.apache.deltaspike.test.core.api.exclude.AlwaysActiveBean;
import org.apache.deltaspike.test.core.api.exclude.CustomExpressionBasedBean;
import org.apache.deltaspike.test.core.api.exclude.CustomExpressionBasedNoBean;
import org.apache.deltaspike.test.core.api.exclude.NoBean;
import org.apache.deltaspike.test.core.api.exclude.ProdDbBean;
import org.apache.deltaspike.test.core.api.exclude.StdBean;
import  org.apache.deltaspike.test.core.api.temptestutil.ShrinkWrapArchiveUtil;
import org.apache.deltaspike.test.util.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.net.URL;

/**
 * Integration tests for {@link org.apache.deltaspike.core.api.exclude.Exclude}
 */
@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class ExcludeIntegrationTest
{
    /**
     * Deploy package classes with project-stage integration-test
     */
    @Deployment
    public static WebArchive deploy()
    {
        URL deltaSpikeConfig = ExcludeIntegrationTest.class.getClassLoader()
                .getResource("META-INF/apache-deltaspike.properties");

        URL testExtensionsFileUrl = ExcludeIntegrationTest.class.getClassLoader()
                .getResource("META-INF/services/test.javax.enterprise.inject.spi.Extension");

        return ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(ShrinkWrapArchiveUtil.getArchives(null,
                        "META-INF/beans.xml",
                        new String[]{"org.apache.deltaspike.core",
                                "org.apache.deltaspike.integration",
                                "org.apache.deltaspike.test.core.api.exclude"},
                        null))
                .addClass(IntegrationTestProjectStageProducer.class)
                .addAsResource(FileUtils.getFileForURL(deltaSpikeConfig.toString()),
                        "META-INF/apache-deltaspike.properties")
                .addAsResource(FileUtils.getFileForURL(testExtensionsFileUrl.toString()),
                        "META-INF/services/javax.enterprise.inject.spi.Extension")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * check project-stage to ensure the correct config for the other tests in this class
     */
    @Test
    public void checkProjectStage()
    {
        Assert.assertEquals(ProjectStage.IntegrationTest, ProjectStageProducer.getInstance().getProjectStage());
    }

    /**
     * check if this package is included at all
     */
    @Test
    public void simpleCheckOfBeansInPackage()
    {
        AlwaysActiveBean testBean = BeanProvider.getContextualReference(AlwaysActiveBean.class, true);

        Assert.assertNotNull(testBean);
    }

    /**
     * bean is excluded in any case
     */
    @Test
    public void excludeWithoutCondition()
    {
        NoBean noBean = BeanProvider.getContextualReference(NoBean.class, true);

        Assert.assertNull(noBean);
    }

    /**
     * bean excluded in case of project-stage integration-test
     */
    @Test
    public void excludeInCaseOfProjectStageIntegrationTest()
    {
        StdBean stdBean = BeanProvider.getContextualReference(StdBean.class, true);

        Assert.assertNull(stdBean);
    }

    /**
     * bean included in case of project-stage integration-test
     */
    @Test
    public void includedInCaseOfProjectStageIntegrationTest()
    {
        IntegrationTestBean integrationTestBean = BeanProvider.getContextualReference(IntegrationTestBean.class, true);

        Assert.assertNotNull(integrationTestBean);
    }

    /**
     * beans de-/activated via expressions
     */
    @Test
    public void excludedIfExpressionMatch()
    {
        ProdDbBean prodDbBean = BeanProvider.getContextualReference(ProdDbBean.class, true);

        Assert.assertNull(prodDbBean);

        IntegrationTestDbBean integrationTestDbBean =
                BeanProvider.getContextualReference(IntegrationTestDbBean.class, true);

        Assert.assertNotNull(integrationTestDbBean);
    }

    /**
     * bean excluded based on a custom expression syntax
     */
    @Test
    public void excludedBasedOnCustomExpressionSyntax()
    {
        CustomExpressionBasedNoBean noBean =
                BeanProvider.getContextualReference(CustomExpressionBasedNoBean.class, true);

        Assert.assertNull(noBean);
    }

    /**
     * bean included based on a custom expression syntax
     */
    @Test
    public void includedBasedOnCustomExpressionSyntax()
    {
        CustomExpressionBasedBean bean =
                BeanProvider.getContextualReference(CustomExpressionBasedBean.class, true);

        Assert.assertNotNull(bean);
    }
}
