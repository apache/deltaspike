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
package org.apache.deltaspike.data.api.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SimpleQueryInOutMapperBaseTest
{

    @Test
    public void checkDefault() {
        final SimpleQueryInOutMapperBase<String, Integer> mapper = new SimpleQueryInOutMapperBase<String, Integer>()
        {
            @Override
            public Integer toDto(final String result) {
                return result.length();
            }

            @Override
            protected String toEntity(String entity, Integer b) {
                return entity;
            }

            @Override
            protected Object getPrimaryKey(Integer dto)
            {
                return null;
            }

            @Override
            protected String newEntity()
            {
                return "ok";
            }
        };

        assertNull(mapper.mapResult(null));
        assertEquals(2, mapper.mapResult("ab"));

        final List<Integer> collection = List.class.cast(mapper.mapResultList(Arrays.asList(null, "a", "bc")));
        assertEquals(3, collection.size());
        assertEquals(Arrays.asList(null, 1, 2), collection);

        assertFalse(mapper.mapsParameter(null));
        assertFalse(mapper.mapsParameter("foo"));
        assertFalse(mapper.mapsParameter(true));
        assertFalse(mapper.mapsParameter(2));
        assertTrue(mapper.mapsParameter(new Object() {
        }));

        // this test is particular cause we refuse String but forcing call to it we having the mapper behavior
        // but at least we test what is expected!
        assertEquals("ok", mapper.mapParameter(2));
    }

}
