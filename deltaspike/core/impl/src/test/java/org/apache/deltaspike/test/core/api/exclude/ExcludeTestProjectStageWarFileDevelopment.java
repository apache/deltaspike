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
package org.apache.deltaspike.test.core.api.exclude;


import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

/**
 * Tests for {@link org.apache.deltaspike.core.api.exclude.Exclude}
 */
@RunWith(Arquillian.class)
public class ExcludeTestProjectStageWarFileDevelopment extends ExcludeTestProjectStageDevelopment
{
    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        String simpleName = ExcludeTestProjectStageWarFileDevelopment.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        System.setProperty("org.apache.deltaspike.ProjectStage", "Development");
        ProjectStageProducer.setProjectStage(null);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "excludeTestProjectStageDevelopmentTest.jar")
                .addPackage(ExcludeTestProjectStageWarFileDevelopment.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
