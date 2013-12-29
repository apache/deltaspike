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
package org.apache.deltaspike.test.core.impl.resourceloader;


import org.apache.deltaspike.core.api.literal.ExternalResourceLiteral;
import org.apache.deltaspike.core.api.resourceloader.ClasspathStorage;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.apache.deltaspike.test.utils.CdiContainerUnderTest;
import org.apache.deltaspike.test.utils.CdiImplementation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@RunWith(Arquillian.class)
public class ClasspathResourceTest
{
    @Deployment
    public static Archive<?> createResourceLoaderArchive()
    {
        Class versionDependentImplementation = Cdi11Bean.class;
        if (isOwbForCdi10())
        {
            versionDependentImplementation = Cdi10Bean.class;
        }

        Archive<?> arch = ShrinkWrap.create(WebArchive.class, ClasspathResourceTest.class.getSimpleName() + ".war")
                .addClass(TestResourceHolder.class)
                .addClass(versionDependentImplementation)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .add(new StringAsset("some.propertykey = somevalue"), "WEB-INF/classes/testconfig.properties")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive());
        return arch;
    }

    @Inject
    private TestResourceHolder testResourceHolder;

    @Test
    public void testInputStream() throws IOException
    {
        Assume.assumeTrue(!isOwbForCdi10());

        Assert.assertNotNull(testResourceHolder.getInputStream());
        Properties p = new Properties();
        p.load(testResourceHolder.getInputStream());
        Assert.assertEquals("somevalue", p.getProperty("some.propertykey", "wrong answer"));
    }

    @Test
    public void testProperties()
    {
        Assume.assumeTrue(!isOwbForCdi10());

        Assert.assertEquals("somevalue",
            testResourceHolder.getProperties().getProperty("some.propertykey", "wrong answer"));
    }

    @Test(expected = RuntimeException.class)
    public void testAmbiguousFileLookup()
    {
        Assume.assumeTrue(!isOwbForCdi10());

        testResourceHolder.getInputStreamInstance()
            .select(new ExternalResourceLiteral(ClasspathStorage.class, "META-INF/beans.xml")).get();
    }

    @Test
    public void testSuccessfulAmbiguousLookup()
    {
        Assume.assumeTrue(!isOwbForCdi10());
        //note, we only test this on classpath, since File impl is always getting 1.
        List<InputStream> streams = testResourceHolder.getInputStreams();
        Assert.assertTrue(streams.size() > 1); //the count is different on as7 compared to the standalone setup
    }

    private static boolean isOwbForCdi10()
    {
        return CdiContainerUnderTest.isCdiVersion(CdiImplementation.OWB11) || CdiContainerUnderTest.isCdiVersion(CdiImplementation.OWB12);
    }
}
