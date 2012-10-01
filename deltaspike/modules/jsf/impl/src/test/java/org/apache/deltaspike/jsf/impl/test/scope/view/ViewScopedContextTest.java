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
package org.apache.deltaspike.jsf.impl.test.scope.view;


import org.apache.deltaspike.jsf.impl.test.scope.view.util.ArchiveUtils;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test for the DeltaSpike ViewScoped context
 */
@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class ViewScopedContextTest
{
    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive
     * cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "viewScopedContextTest.jar")
                .addPackage(ViewScopedContextTest.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap
                .create(WebArchive.class, "viewScopedContextTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }


    @Test
    public void testViewScopedContext()
    {
        //X TODO how to best test a JSF app?
    }
}
