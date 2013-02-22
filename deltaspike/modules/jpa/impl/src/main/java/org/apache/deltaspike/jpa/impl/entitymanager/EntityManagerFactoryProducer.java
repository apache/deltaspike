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
package org.apache.deltaspike.jpa.impl.entitymanager;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.deltaspike.test.jpa.api.entitymanager.PersistenceConfigurationProvider;
import org.apache.deltaspike.test.jpa.api.entitymanager.PersistenceUnitName;


/**
 *  TODO
 */
public class EntityManagerFactoryProducer
{
    private static final Logger LOG = Logger.getLogger(EntityManagerFactoryProducer.class.getName());


    private @Inject PersistenceConfigurationProvider persistenceConfigurationProvider;


    @Produces
    @Dependent
    @PersistenceUnitName("any") // the value is nonbinding, thus this is just a dummy parameter here
    public EntityManagerFactory createEntityManagerFactoryForUnit(InjectionPoint injectionPoint)
    {
        PersistenceUnitName unitNameAnnotation = injectionPoint.getAnnotated().getAnnotation(PersistenceUnitName.class);

        if (unitNameAnnotation == null)
        {
            LOG.warning("@PersisteneUnitName annotation could not be found at EntityManagerFactory injection point!");

            return null;
        }

        String unitName = unitNameAnnotation.value();

        Properties properties = persistenceConfigurationProvider.getEntityManagerFactoryConfiguration(unitName);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(unitName, properties);

        return emf;
    }
}
