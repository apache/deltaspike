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
package org.apache.deltaspike.data.impl.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import javax.inject.Inject;

import org.apache.deltaspike.data.api.QueryInvocationException;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.service.SimpleRepositoryWithEntityManager;
import org.apache.deltaspike.data.test.service.SimpleRepositoryWithEntityManagerResolver;
import org.apache.deltaspike.data.test.service.Simplistic;
import org.apache.deltaspike.data.test.service.SimplisticEntityManagerResolver;
import org.apache.deltaspike.data.test.util.TestDeployments;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class EntityManagerTest
{

    @Deployment
    public static Archive<?> deployment()
    {
        return TestDeployments.initDeployment()
                .addClasses(SimpleRepositoryWithEntityManager.class,
                        SimpleRepositoryWithEntityManagerResolver.class,
                        QualifiedEntityManagerTestProducer.class,
                        NonQualifiedEntityManagerTestProducer.class,
                        Simplistic.class, SimplisticEntityManagerResolver.class);
    }

    @Inject
    private SimpleRepositoryWithEntityManager repoWithDefaultEm;

    @Inject
    private SimpleRepositoryWithEntityManagerResolver repoWithInjection;

    @Test
    public void should_use_default_entity_manager()
    {
        // when
        List<Simple> result = repoWithDefaultEm.findByName("testUseQualifiedEntityManager");

        // then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /*
     * Injected EM throws UnsupportedOperationException on all methods.
     * Shortcutting the creation of multiple PUs (lazy guy...)
     */
    @Test(expected = QueryInvocationException.class)
    public void should_use_entity_manager_from_resolver()
    {
        // when
        repoWithInjection.findByName("testUseQualifiedEntityManager");

        // then
        fail("Fake EM should have thrown Exception");
    }

}
