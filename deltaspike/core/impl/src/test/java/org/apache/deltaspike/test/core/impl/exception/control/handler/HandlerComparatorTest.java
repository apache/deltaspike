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

package org.apache.deltaspike.test.core.impl.exception.control.handler;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.test.core.impl.exception.control.extension.Account;
import org.apache.deltaspike.test.core.impl.exception.control.extension.CatchQualifier;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

//TODO re-activate
//@RunWith(Arquillian.class)
public class HandlerComparatorTest
{
    @Deployment(name = "HandlerComparatorTest")
    public static Archive<?> createTestArchive()
    {
        return ShrinkWrap
                .create(WebArchive.class, "handlerComparator.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(ExtensionExceptionHandler.class, Account.class,
                        org.apache.deltaspike.test.core.impl.exception.control.extension.Arquillian.class,
                        CatchQualifier.class);
    }

    @Inject
    private BeanManager bm;

    //TODO re-activate
    //@Test
    public void assertOrderIsCorrectDepthFirst()
    {
        /*
        List<HandlerMethod<? extends Throwable>> handlers = new ArrayList<HandlerMethod<? extends Throwable>>(
                ExceptionControlExtension.createStorage().getHandlersForException(
                        IllegalArgumentException.class, bm, Collections.<Annotation>emptySet(), false));

        System.out.println(handlers);

        assertEquals("catchThrowable", ((HandlerMethodImpl<?>) handlers.get(0)).getJavaMethod().getName());
        assertEquals("catchThrowableP20", ((HandlerMethodImpl<?>) handlers.get(1)).getJavaMethod().getName());
        assertEquals("catchRuntime", ((HandlerMethodImpl<?>) handlers.get(2)).getJavaMethod().getName());
        assertEquals("catchIAE", ((HandlerMethodImpl<?>) handlers.get(3)).getJavaMethod().getName());
        */
    }

    //TODO re-activate
    //@Test
    public void assertOrderIsCorrectBreadthFirst()
    {
        /*
        List<HandlerMethod<? extends Throwable>> handlers = new ArrayList<HandlerMethod<? extends Throwable>>(
                ExceptionControlExtension.createStorage().getHandlersForException(
                        Exception.class, bm, Collections.<Annotation>emptySet(), true));

        assertThat(handlers.size(), is(4));
        */
    }
}
