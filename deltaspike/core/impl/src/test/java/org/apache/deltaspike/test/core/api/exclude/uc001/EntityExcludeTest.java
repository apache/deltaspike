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
package org.apache.deltaspike.test.core.api.exclude.uc001;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.apache.deltaspike.test.utils.BeansXmlUtil.BEANS_XML_ALL;

@RunWith(Arquillian.class)
public class EntityExcludeTest
{
    @Deployment
    public static WebArchive deploy()
    {
        String simpleName = EntityExcludeTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        // in case the Arquillian adapter doesn't properly handle resources on the classpath
        ProjectStageProducer.setProjectStage(ProjectStage.Development);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
                .addPackage(EntityExcludeTest.class.getPackage())
                .addAsManifestResource(BEANS_XML_ALL, "beans.xml")
                .addAsResource(new StringAsset("org.apache.deltaspike.ProjectStage = Development"),
                    "apache-deltaspike.properties"); // when deployed on some remote container;

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(BEANS_XML_ALL, "beans.xml");
    }

    @AfterClass
    public static void resetProjectStage() {
        ProjectStageProducer.setProjectStage(null);
    }

    @Test
    public void entityWithoutExclusion()
    {
        Entity1 entity1 = BeanProvider.getContextualReference(Entity1.class, true);
        Assert.assertNotNull(entity1);
    }

    @Test
    public void excludedEntity()
    {
        Entity2 entity2 = BeanProvider.getContextualReference(Entity2.class, true);
        Assert.assertNull(entity2);
    }

    //TODO discuss it - if we don't need it, we can use @Inherited
    @Test
    public void excludedBaseClassWithoutInheritance()
    {
        BaseEntity3 baseEntity3 = BeanProvider.getContextualReference(BaseEntity3.class, true);
        Assert.assertTrue(baseEntity3 instanceof Entity3);

        Entity3 entity3 = BeanProvider.getContextualReference(Entity3.class, true);
        Assert.assertNotNull(entity3);
    }
}
