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

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.impl.exclude.extension.ExcludeExtension;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.test.core.impl.activation.TestClassDeactivator;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.apache.deltaspike.test.util.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Tests for {@link org.apache.deltaspike.core.api.exclude.Exclude}
 */
@RunWith(Arquillian.class)
public class ExcludeWarFileTest extends ExcludeTest
{
    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy() throws IOException
    {
        String simpleName = ExcludeWarFileTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        // in case the Arquillian adapter doesn't properly handle resources on the classpath
        ProjectStageProducer.setProjectStage(ProjectStage.Production);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "excludeTest.jar")
                .addPackage(ExcludeWarFileTest.class.getPackage())
                .addPackage(TestClassDeactivator.class.getPackage())
                .addAsManifestResource(new StringAsset(getConfigContent()),
                    "apache-deltaspike.properties") // when deployed on some remote container;
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class, ExcludeExtension.class);
    }

    public static String getConfigContent() throws IOException
    {
        byte[] configContent = Files.readAllBytes(FileUtils.getFileForURL(ExcludeWarFileTest.class.getClassLoader()
                .getResource("META-INF/apache-deltaspike.properties").toString()).toPath());
        return (new String(configContent, StandardCharsets.UTF_8) +
            "\norg.apache.deltaspike.ProjectStage = Production")
                .replace("deltaspike.interdyn.enabled=true", "deltaspike.interdyn.enabled=false");
    }

    @AfterClass
    public static void resetProjectStage() {
        ProjectStageProducer.setProjectStage(null);
    }
}
