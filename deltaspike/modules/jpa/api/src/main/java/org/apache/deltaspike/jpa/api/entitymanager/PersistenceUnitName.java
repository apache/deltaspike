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
package org.apache.deltaspike.jpa.api.entitymanager;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

/**
 * The name of the PersistenceUnit to get picked up by the
 * EntityManagerFactoryProducer.
 *
 * The EntityManagerFactoryProducer will in turn use the
 * {@link org.apache.deltaspike.jpa.spi.entitymanager.PersistenceConfigurationProvider}
 * to pick up the properties to be used for creating the {@code }EntityManagerFactory}.
 */
@Target( { TYPE, METHOD, PARAMETER, FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface PersistenceUnitName
{
    /**
     * @return the name of the persistence unit.
     */
    @Nonbinding
    String value();
}
