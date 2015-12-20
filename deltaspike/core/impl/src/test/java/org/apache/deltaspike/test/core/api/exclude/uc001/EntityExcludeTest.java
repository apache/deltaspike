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

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EntityExcludeTest
{
    @Deployment
    public static WebArchive deploy()
    {
        String simpleName = EntityExcludeTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        System.setProperty("org.apache.deltaspike.ProjectStage", "Development");
        ProjectStageProducer.setProjectStage(null);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
                .addPackage(EntityExcludeTest.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
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
