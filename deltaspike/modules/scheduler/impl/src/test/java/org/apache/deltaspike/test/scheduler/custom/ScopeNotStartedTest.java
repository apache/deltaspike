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
package org.apache.deltaspike.test.scheduler.custom;

import junit.framework.Assert;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.scheduler.spi.Scheduler;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Arquillian.class)
public class ScopeNotStartedTest
{
    //TODO even though this test exists, the tests in this module don't execute for quartz.
    @Deployment
    public static WebArchive deploy()
    {
        String simpleName = ScopeNotStartedTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "scopeNotStartedTest.jar")
                .addPackage(CustomSchedulerWarFileTest.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new StringAsset(MockedScheduler.class.getName()),
                        "META-INF/services/" + Scheduler.class.getName())
                .addAsResource(new StringAsset(CustomDeactivatedConfigSource.class.getName()),
                        "META-INF/services/" + ConfigSource.class.getName());

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndSchedulerArchive())
                .addAsLibraries(ArchiveUtils.getContextControlForDeployment())
                .addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve(
                        "org.quartz-scheduler:quartz")
                        .withTransitivity()
                        .asFile())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private Scheduler scheduler;

    @Inject
    private TestJobManager testJobManager;

    @Test
    public void testRegisterBadJob()
    {
        Assert.assertTrue(testJobManager.isStarted());

        this.scheduler.registerNewJob(ManualJob.class);
        Assert.assertEquals(2, testJobManager.getRegisteredJobs().size());
        Assert.assertEquals(2, testJobManager.getRunningJobs().size());

    }
}
