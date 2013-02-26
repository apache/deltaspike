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
package org.apache.deltaspike.jpa.spi.entitymanager;

import java.util.Properties;

/**
 * Provide the configuration for the EntityManagerFactory
 * which gets produced with a given {@link org.apache.deltaspike.jpa.api.entitymanager.PersistenceUnitName}.
 *
 * By default we provide a configuration which con be configured
 * differently depending on the <i>-DdatabaseVendor</i> and the
 * {@link org.apache.deltaspike.core.api.projectstage.ProjectStage}
 */
public interface PersistenceConfigurationProvider
{

    /**
     * @param persistenceUnitName the name of the persistence unit in persistence.xml
     * @return the additional Properties from the configuration.
     */
    Properties getEntityManagerFactoryConfiguration(String persistenceUnitName);
}
