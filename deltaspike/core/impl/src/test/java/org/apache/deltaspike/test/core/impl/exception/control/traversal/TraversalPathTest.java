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

package org.apache.deltaspike.test.core.impl.exception.control.traversal;

import org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import static org.junit.Assert.assertArrayEquals;

@RunWith(Arquillian.class)
public class TraversalPathTest
{
    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> createTestArchive()
    {
        return ShrinkWrap
                .create(WebArchive.class, "traversalPath.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackage(TraversalPathTest.class.getPackage());
    }

    /**
     * Tests SEAMCATCH-32, see JIRA for more information about this test. https://issues.jboss.org/browse/SEAMCATCH-32
     */
    @Test
    public void testTraversalPathOrder()
    {
        // create an exception stack E1 -> E2 -> E3
        Exceptions.Exception1 exception = new Exceptions.Exception1(new
                Exceptions.Exception2(new Exceptions.Exception3()));

        manager.fireEvent(new ExceptionToCatchEvent(exception));

        /*
            handleException3SuperclassBF
            handleException3BF
            handleException3DF
            handleException3SuperclassDF
            handleException2BF
            handleException2DF
            handleException1BF
            handleException1DF
        */
        Object[] expectedOrder = {1, 2, 3, 4, 5, 6, 7, 8};
        assertArrayEquals(expectedOrder, ExceptionHandlerMethods.getExecutionorder().toArray());
    }
}
