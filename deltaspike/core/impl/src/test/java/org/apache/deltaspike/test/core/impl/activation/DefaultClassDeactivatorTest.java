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
package org.apache.deltaspike.test.core.impl.activation;

import org.apache.deltaspike.core.impl.activation.DefaultClassDeactivator;
import org.apache.deltaspike.core.spi.activation.ClassDeactivator;
import org.apache.deltaspike.test.category.DeltaSpikeTest;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@RunWith(Arquillian.class)
public class DefaultClassDeactivatorTest extends ClassDeactivationTest
{
    @Deployment
    public static WebArchive deploy()
    {
        String simpleName = DefaultClassDeactivatorTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        StringBuilder dsPropsBuilder = new StringBuilder();
        dsPropsBuilder.append(ClassDeactivator.class.getName()).append("=")
                .append(DefaultClassDeactivator.class.getName()).append("\n")
                // this gets picked up on app servers, not when using an embedded impl
                .append(DefaultClassDeactivator.KEY_PREFIX).append(DeactivatedClass.class.getName()).append("=true").append("\n");


        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "testClassDeactivationTest.jar")
                .addPackage(ClassDeactivationWarFileTest.class.getPackage())
                .addClass(DefaultClassDeactivator.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsResource(new StringAsset(dsPropsBuilder.toString()), DeltaSpikeTest.DELTASPIKE_PROPERTIES)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testViaInstantiatedCopyTheyAreDeactivated()
    {
        DefaultClassDeactivator defaultClassDeactivator = new DefaultClassDeactivator();
        Boolean activated = defaultClassDeactivator.isActivated(ActivatedClass.class);
        assertNull(activated);

        Boolean deactivated = defaultClassDeactivator.isActivated(DeactivatedClass.class);
        assertFalse(deactivated);
    }
}
