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
package org.apache.deltaspike.test.jpa.api.entitymanager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.deltaspike.jpa.api.entitymanager.PersistenceUnitName;

/**
 * Sample producer for a &#064;SampleDb EntityManager.
 */
@ApplicationScoped
@SuppressWarnings("unused")
public class SampleEntityManagerProducer
{
    @Inject
    @PersistenceUnitName("testPersistenceUnit")
    private EntityManagerFactory emf;

    @Produces
    @RequestScoped
    @SampleDb
    public EntityManager createEntityManager()
    {
        return emf.createEntityManager();
    }

    public void closeEm(@Disposes @SampleDb EntityManager em)
    {
        em.close();
    }
}
