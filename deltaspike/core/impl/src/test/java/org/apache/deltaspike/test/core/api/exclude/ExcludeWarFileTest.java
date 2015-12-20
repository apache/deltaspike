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

import org.apache.deltaspike.core.impl.exclude.extension.ExcludeExtension;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.test.core.impl.activation.TestClassDeactivator;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.apache.deltaspike.test.util.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import java.net.URL;

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
    public static WebArchive deploy()
    {
        String simpleName = ExcludeWarFileTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        System.setProperty("org.apache.deltaspike.ProjectStage", "Production");
        ProjectStageProducer.setProjectStage(null);

        URL fileUrl = ExcludeWarFileTest.class.getClassLoader()
                .getResource("META-INF/apache-deltaspike.properties");

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "excludeTest.jar")
                .addPackage(ExcludeWarFileTest.class.getPackage())
                .addPackage(TestClassDeactivator.class.getPackage())
                .addAsManifestResource(FileUtils.getFileForURL(fileUrl.toString()))
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class, ExcludeExtension.class);
    }
}
