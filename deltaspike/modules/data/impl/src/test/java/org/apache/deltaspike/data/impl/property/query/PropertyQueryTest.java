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
package org.apache.deltaspike.data.impl.property.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.deltaspike.data.impl.property.Property;
import org.apache.deltaspike.data.impl.property.query.NamedPropertyCriteria;
import org.apache.deltaspike.data.impl.property.query.PropertyQueries;
import org.apache.deltaspike.data.impl.property.query.PropertyQuery;
import org.apache.deltaspike.data.impl.property.query.TypedPropertyCriteria;
import org.junit.Test;

/**
 * Validate the property query mechanism.
 */
public class PropertyQueryTest
{
    /**
     * Querying for a single result with a criteria that matches multiple properties should throw an exception.
     *
     * @see PropertyQuery#getSingleResult()
     */
    @Test(expected = RuntimeException.class)
    public void testNonUniqueSingleResultThrowsException()
    {
        PropertyQuery<String> q = PropertyQueries.<String> createQuery(Person.class);
        q.addCriteria(new TypedPropertyCriteria(String.class));
        q.getSingleResult();
    }

    /**
     * Querying for a single result with a criteria that does not match any properties should throw an exception.
     *
     * @see PropertyQuery#getSingleResult()
     */
    @Test(expected = RuntimeException.class)
    public void testEmptySingleResultThrowsException()
    {
        PropertyQuery<String> q = PropertyQueries.<String> createQuery(Person.class);
        q.addCriteria(new TypedPropertyCriteria(Integer.class));
        q.getSingleResult();
    }

    /**
     * Querying for a single result with a criterai that matches exactly one property should return the property.
     *
     * @see PropertyQuery#getSingleResult()
     */
    @Test
    public void testSingleResult()
    {
        PropertyQuery<String> q = PropertyQueries.<String> createQuery(Person.class);
        q.addCriteria(new NamedPropertyCriteria("name"));
        Property<String> p = q.getSingleResult();
        assertNotNull(p);
        Person o = new Person();
        o.setName("Trap");
        assertEquals("Trap", p.getValue(o));
    }

    public static class Person
    {

        private String name;
        private String title;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }
    }

}
