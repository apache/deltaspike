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
package org.apache.deltaspike.data.impl.meta;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MethodPrefixTest
{
    @Test
    public void shouldParseArbitraryMethodFindPrefix()
    {
        RepositoryMethodPrefix methodPrefix = new RepositoryMethodPrefix("","findTop20ByName");

        String resultingQuery = methodPrefix.removePrefix("findTop20ByName");

        assertEquals("Name", resultingQuery);
    }

    @Test
    public void shouldParseFirst20MethodFindPrefix()
    {
        RepositoryMethodPrefix methodPrefix = new RepositoryMethodPrefix("","findFirst20ByName");

        String resultingQuery = methodPrefix.removePrefix("findFirst20ByName");

        assertEquals("Name", resultingQuery);
    }

    @Test
    public void shouldParseDefinedMaxResults()
    {
        RepositoryMethodPrefix methodPrefix = new RepositoryMethodPrefix("","findFirst20ByName");

        int maxResults = methodPrefix.getDefinedMaxResults();

        assertEquals(20, maxResults);
    }

    @Test
    public void shouldNotParseNonMatchingMethodName()
    {
        RepositoryMethodPrefix methodPrefix = new RepositoryMethodPrefix("","findAnyByName");

        int maxResults = methodPrefix.getDefinedMaxResults();

        assertEquals(0, maxResults);
    }

    @Test
    public void shouldParseOrderByEmailDesc()
    {
        RepositoryMethodPrefix methodPrefix = new RepositoryMethodPrefix("","findFirst3OrderByEmailDesc");

        String resultingQuery = methodPrefix.removePrefix("findFirst3OrderByEmailDesc");

        assertEquals("OrderByEmailDesc", resultingQuery);
    }

    @Test
    public void shouldParseOrderByEmail()
    {
        RepositoryMethodPrefix methodPrefix = new RepositoryMethodPrefix("","findFirst3OrderByEmail");

        String resultingQuery = methodPrefix.removePrefix("findFirst3OrderByEmail");

        assertEquals("OrderByEmail", resultingQuery);
    }

    @Test
    public void shouldParseFindAllOrderByName()
    {
        RepositoryMethodPrefix methodPrefix = new RepositoryMethodPrefix("","findAllOrderByName");

        String resultingQuery = methodPrefix.removePrefix("findAllOrderByName");

        assertEquals("OrderByName", resultingQuery);
    }

}