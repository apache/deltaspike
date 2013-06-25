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
package org.apache.deltaspike.data.impl.audit;

import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.deltaspike.data.api.audit.CurrentUser;
import org.apache.deltaspike.data.impl.audit.AuditEntityListener;
import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.AuditedEntity;
import org.apache.deltaspike.data.test.domain.Principal;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.junit.Test;

public class AuditEntityListenerTest extends TransactionalTestCase
{

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
                .addPackage(AuditEntityListener.class.getPackage())
                .addAsWebInfResource("test-orm.xml", ArchivePaths.create("classes/META-INF/orm.xml"))
                .addPackage(AuditedEntity.class.getPackage());
    }

    @PersistenceContext
    private EntityManager entityManager;

    private final String who = "test999";
    private final Principal principal = new Principal(who);

    @Produces
    @CurrentUser
    public String who()
    {
        return who;
    }

    @Produces
    @CurrentUser
    public Principal entity() throws Exception
    {
        try
        {
            entityManager.persist(principal);
        }
        catch (Throwable e)
        {
        }
        return principal;
    }

    @Test
    public void should_set_creation_date() throws Exception
    {
        // given
        AuditedEntity entity = new AuditedEntity();

        // when
        entityManager.persist(entity);
        entityManager.flush();

        // then
        assertNotNull(entity.getCreated());
        assertNotNull(entity.getModified());
        assertEquals(entity.getCreated().getTime(), entity.getModified());
    }

    @Test
    public void should_set_modification_date() throws Exception
    {
        // given
        AuditedEntity entity = new AuditedEntity();
        entityManager.persist(entity);
        entityManager.flush();

        // when
        entity = entityManager.find(AuditedEntity.class, entity.getId());
        entity.setName("test");
        entityManager.flush();

        // then
        assertNotNull(entity.getGregorianModified());
        assertNotNull(entity.getTimestamp());
    }

    @Test
    public void should_set_changing_principal()
    {
        // given
        AuditedEntity entity = new AuditedEntity();

        // when
        entityManager.persist(entity);
        entityManager.flush();

        // then
        assertNotNull(entity.getChanger());
        assertEquals(who, entity.getChanger());
        assertNotNull(entity.getPrincipal());
        assertEquals(who, entity.getPrincipal().getName());
    }

    @Override
    protected EntityManager getEntityManager()
    {
        return entityManager;
    }

}
