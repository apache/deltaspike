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
package org.apache.deltaspike.data.impl.spi;

import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.service.MyEntityRepository;
import org.apache.deltaspike.data.test.service.MyEntityRepositoryDelegate;
import org.apache.deltaspike.data.test.service.MySimpleRepository;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(WebProfileCategory.class)
public class CdiQuerySpiTest extends TransactionalTestCase
{

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
                .addClasses(MySimpleRepository.class,
                        MyEntityRepository.class,
                        MyEntityRepositoryDelegate.class)
                .addPackage(Simple.class.getPackage());
    }

    @Inject
    private MySimpleRepository repo;

    @Test
    public void should_call_delegate()
    {
        // given
        Simple simple = new Simple("test_call_delegate");

        // when
        simple = repo.saveAndFlushAndRefresh(simple);

        // then
        assertNotNull(simple.getId());
    }
}
