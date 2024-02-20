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

import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.Simple5;
import org.apache.deltaspike.data.test.service.ExtendedRepositoryAbstractInherited;
import org.apache.deltaspike.data.test.service.ExtendedRepositoryAbstractIntermediate;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.inject.Inject;

@Category(WebProfileCategory.class)
public class EntityRepositoryHandlerInheritedTest extends TransactionalTestCase
{
    
    private static final Asset beansXml = new StringAsset("<beans bean-discovery-mode=\"all\"/>");

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment(true, beansXml)
                .addClasses(ExtendedRepositoryAbstractIntermediate.class, 
                        ExtendedRepositoryAbstractInherited.class,
                        NamedQualifiedEntityManagerTestProducer.class)
                .addPackage(Simple.class.getPackage());
    }

    @Inject
    private ExtendedRepositoryAbstractInherited repo;

    @Test
    public void should_return_entity_name()
    {
        final String entityName = repo.getEntityName();

        assertEquals("EntitySimple5", entityName);
    }

    @Test
    public void should_return_null_entity()
    {
        final Simple5 entity = repo.findByIdAndName(-1L, "any");

        assertNull(entity);
    }

}
