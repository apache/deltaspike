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
package org.apache.deltaspike.data.impl.meta.unit;

import static org.junit.Assert.assertEquals;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.mapped.MappedOne;
import org.apache.deltaspike.data.test.service.MappedOneRepository;
import org.apache.deltaspike.data.test.util.TestDeployments;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class OrmXmlBasedRepositoryTest extends TransactionalTestCase
{

    @Deployment
    public static Archive<?> deployment()
    {
        return TestDeployments
                .initDeployment("(.*mapped.*)|(.*test.*)")
                .addClasses(MappedOneRepository.class)
                .addAsLibraries(
                        ShrinkWrap.create(JavaArchive.class, "domain.jar")
                                .addPackage(MappedOne.class.getPackage())
                                .addAsResource("test-custom-orm.xml", ArchivePaths.create("META-INF/custom-orm.xml"))
                )
                .addAsWebInfResource("test-mapped-persistence.xml",
                        ArchivePaths.create("classes/META-INF/persistence.xml"))
                .addAsWebInfResource("test-default-orm.xml", ArchivePaths.create("classes/META-INF/orm.xml"));
    }

    @Produces
    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private MappedOneRepository mappedOneRepository;

    @Test
    public void should_find_by()
    {
        // given
        MappedOne one = createMappedOne("shouldFindBy");

        // when
        MappedOne byPk = mappedOneRepository.findBy(one.getId());
        MappedOne byName = mappedOneRepository.findByName("shouldFindBy");

        // then
        assertEquals(one.getId(), byPk.getId());
        assertEquals(one.getId(), byName.getId());
    }

    @Override
    protected EntityManager getEntityManager()
    {
        return entityManager;
    }

    private MappedOne createMappedOne(String name)
    {
        MappedOne result = new MappedOne(name);
        entityManager.persist(result);
        entityManager.flush();
        return result;
    }

}
