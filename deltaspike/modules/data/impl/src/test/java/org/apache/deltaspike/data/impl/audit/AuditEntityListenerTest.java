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
import static org.junit.Assert.assertNull;

import javax.enterprise.inject.Produces;

import org.apache.deltaspike.data.api.audit.CurrentUser;
import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.AuditedEntity;
import org.apache.deltaspike.data.test.domain.Principal;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(WebProfileCategory.class)
public class AuditEntityListenerTest extends TransactionalTestCase
{

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
                .addPackage(AuditEntityListener.class.getPackage())
                .addAsWebInfResource("test-orm.xml", "classes/META-INF/orm.xml")
                .addPackage(AuditedEntity.class.getPackage());
    }

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
            getEntityManager().persist(principal);
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
        getEntityManager().persist(entity);
        getEntityManager().flush();

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
        getEntityManager().persist(entity);
        getEntityManager().flush();

        // when
        entity = getEntityManager().find(AuditedEntity.class, entity.getId());
        entity.setName("test");
        getEntityManager().flush();

        // then
        assertNotNull(entity.getGregorianModified());
        assertNotNull(entity.getTimestamp());
    }

    @Test
    public void should_set_changing_principal()
    {
        // given
        AuditedEntity entity = new AuditedEntity();
        getEntityManager().persist(entity);
        getEntityManager().flush();

        // when
        entity = getEntityManager().find(AuditedEntity.class, entity.getId());
        entity.setName("test");
        getEntityManager().flush();

        // then
        assertNotNull(entity.getChanger());
        assertEquals(who, entity.getChanger());
        assertNotNull(entity.getPrincipal());
        assertEquals(who, entity.getPrincipal().getName());
        assertNotNull(entity.getChangerOnly());
        assertEquals(who, entity.getChangerOnly());
        assertNotNull(entity.getChangerOnlyPrincipal());
        assertEquals(who, entity.getChangerOnlyPrincipal().getName());
    }

    @Test
    public void should_set_creating_principal()
    {
        // given
        AuditedEntity entity = new AuditedEntity();

        // when
        getEntityManager().persist(entity);
        getEntityManager().flush();

        // then
        assertNotNull(entity.getCreator());
        assertEquals(who, entity.getCreator());
        assertNotNull(entity.getCreatorPrincipal());
        assertEquals(who, entity.getCreatorPrincipal().getName());
        assertNotNull(entity.getChanger());
        assertEquals(who, entity.getChanger());
        assertNotNull(entity.getPrincipal());
        assertEquals(who, entity.getPrincipal().getName());
        assertNull(entity.getChangerOnly());
        assertNull(entity.getChangerOnlyPrincipal());
    }
}
