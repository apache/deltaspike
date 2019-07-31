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

import org.apache.deltaspike.data.api.audit.CreatedBy;
import org.apache.deltaspike.data.impl.property.Property;
import org.apache.deltaspike.data.test.domain.AuditedEntity;
import org.apache.deltaspike.data.test.domain.Principal;
import org.apache.deltaspike.data.test.domain.Simple;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class PrincipalProviderTest {

    public static class MockPrincipalProvider extends PrincipalProvider {
        private final String who;

        MockPrincipalProvider(String who) {
            this.who = who;
        }

        @Override
        protected Object resolvePrincipal(Object entity, Property<Object> property)
        {
            if (property.getJavaClass().isAssignableFrom(Principal.class))
            {
                return new Principal(who);
            }
            return who;
        }
    }

    @Test
    public void should_set_users_for_creation()
    {
        // given
        String creator = "creator";
        MockPrincipalProvider provider = new MockPrincipalProvider(creator);
        AuditedEntity entity = new AuditedEntity();

        // when
        provider.prePersist(entity);

        // then
        assertNotNull(entity.getCreator());
        assertNotNull(entity.getCreatorPrincipal());
        assertNotNull(entity.getChanger());
        assertEquals(entity.getCreator(), creator);
        assertEquals(entity.getCreatorPrincipal().getName(), creator);
        assertEquals(entity.getChanger(), creator);
        assertNull(entity.getChangerOnly());
        assertNull(entity.getChangerOnlyPrincipal());
    }

    @Test
    public void should_set_users_for_update()
    {
        // given
        String changer = "changer";
        MockPrincipalProvider provider = new MockPrincipalProvider(changer);
        AuditedEntity entity = new AuditedEntity();

        // when
        provider.preUpdate(entity);

        // then
        assertNotNull(entity.getChanger());
        assertNotNull(entity.getChangerOnly());
        assertNotNull(entity.getChangerOnlyPrincipal());
        assertEquals(entity.getChanger(), changer);
        assertEquals(entity.getChangerOnly(), changer);
        assertEquals(entity.getChangerOnlyPrincipal().getName(), changer);
        assertNull(entity.getCreator());
        assertNull(entity.getCreatorPrincipal());
    }

    @Test
    public void should_not_fail_on_non_audited_entity()
    {
        // given
        Simple entity = new Simple("should_not_fail_on_non_audited_entity");

        // when
        PrincipalProvider provider = new MockPrincipalProvider("");
        provider.prePersist(entity);
        provider.preUpdate(entity);

        // then finish the test
    }

    @Test(expected = AuditPropertyException.class)
    public void should_fail_on_invalid_entity()
    {
        // given
        PrincipalProviderTest.InvalidEntity entity = new PrincipalProviderTest.InvalidEntity();

        // when
        new MockPrincipalProvider("").prePersist(entity);

        // then
        fail();
    }

    private static class InvalidEntity
    {
        @CreatedBy
        private Date nonUser;
    }
}
