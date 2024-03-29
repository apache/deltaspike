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
package org.apache.deltaspike.test.jpa.api.transactional.multipleinjection.manual;

import org.apache.deltaspike.test.jpa.api.shared.First;
import org.apache.deltaspike.test.jpa.api.shared.Second;
import org.apache.deltaspike.test.jpa.api.shared.TestEntityManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class TestEntityManagerProducer
{
    private TestEntityManager defaultEntityManager = new TestEntityManager();

    private TestEntityManager firstEntityManager = new TestEntityManager();

    private TestEntityManager secondEntityManager = new TestEntityManager();

    @Produces
    @RequestScoped
    protected EntityManager defaultEntityManager()
    {
        return defaultEntityManager;
    }

    @Produces
    @RequestScoped
    @First
    protected EntityManager firstEntityManager()
    {
        return firstEntityManager;
    }

    @Produces
    @RequestScoped
    @Second
    protected EntityManager secondEntityManager()
    {
        return secondEntityManager;
    }

    public TestEntityManager getDefaultEntityManager()
    {
        return defaultEntityManager;
    }

    public TestEntityManager getFirstEntityManager()
    {
        return firstEntityManager;
    }

    public TestEntityManager getSecondEntityManager()
    {
        return secondEntityManager;
    }
}
