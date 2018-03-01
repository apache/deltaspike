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
package org.apache.deltaspike.test.core.impl.interdyn;

import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Arquillian.class)
public class InterDynTest {
    private final static String CONFIG =
            "# InterDynTest\n" +
            "deltaspike.interdyn.enabled=true\n" +
            "deltaspike.interdyn.rule.1.match=org\\\\.apache\\\\.deltaspike\\\\.test\\\\.core\\\\.impl\\\\.interdyn\\\\.Some.*Service\n" +
            "deltaspike.interdyn.rule.1.annotation=org.apache.deltaspike.core.api.monitoring.InvocationMonitored\n";

    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "InterDynTest.jar")
                .addPackage(SomeTestService.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset(CONFIG), "apache-deltaspike.properties");

        return ShrinkWrap.create(WebArchive.class, "InterDynTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private SomeTestService service;

    @Test
    public void invokeServiceMethods()
    {
        service.enableChecking();

        service.pingA();
        service.pingB();
        service.pingA();
    }
}
