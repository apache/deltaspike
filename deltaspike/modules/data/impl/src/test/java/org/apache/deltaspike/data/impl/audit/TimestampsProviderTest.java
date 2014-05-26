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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.deltaspike.data.api.audit.CreatedOn;
import org.apache.deltaspike.data.test.domain.AuditedEntity;
import org.apache.deltaspike.data.test.domain.Simple;
import org.junit.Test;

public class TimestampsProviderTest
{

    @Test
    public void should_set_dates_for_creation()
    {
        // given
        AuditedEntity entity = new AuditedEntity();

        // when
        new TimestampsProvider().prePersist(entity);

        // then
        assertNotNull(entity.getCreated());
        assertNotNull(entity.getModified());
        assertNull(entity.getGregorianModified());
        assertNull(entity.getTimestamp());
    }

    @Test
    public void should_set_dates_for_update()
    {
        // given
        AuditedEntity entity = new AuditedEntity();

        // when
        new TimestampsProvider().preUpdate(entity);

        // then
        assertNull(entity.getCreated());
        assertNotNull(entity.getModified());
        assertNotNull(entity.getGregorianModified());
        assertNotNull(entity.getTimestamp());
    }

    @Test
    public void should_not_fail_on_non_audited_entity()
    {
        // given
        Simple entity = new Simple("should_not_fail_on_non_audited_entity");

        // when
        TimestampsProvider provider = new TimestampsProvider();
        provider.prePersist(entity);
        provider.preUpdate(entity);

        // then finish the test
    }

    @Test(expected = AuditPropertyException.class)
    public void should_fail_on_invalid_entity()
    {
        // given
        InvalidEntity entity = new InvalidEntity();

        // when
        new TimestampsProvider().prePersist(entity);

        // then
        fail();
    }

    private static class InvalidEntity
    {

        @CreatedOn
        private String nonTemporal;

    }

}
