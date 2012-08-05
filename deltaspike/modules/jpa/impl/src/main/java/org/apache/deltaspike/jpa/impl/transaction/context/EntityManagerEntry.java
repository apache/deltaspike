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
package org.apache.deltaspike.jpa.impl.transaction.context;

import javax.enterprise.inject.Typed;
import javax.persistence.EntityManager;
import java.lang.annotation.Annotation;

/**
 * Stores a {@link EntityManager} and the qualifier
 */
@Typed()
public class EntityManagerEntry
{
    private final EntityManager entityManager;
    //TODO DELTASPIKE-259 - use the annotation itself + calculate a key for #hashCode and #equals
    private Class<? extends Annotation> qualifier;

    public EntityManagerEntry(EntityManager entityManager, Class<? extends Annotation> qualifier)
    {
        this.entityManager = entityManager;
        this.qualifier = qualifier;
    }

    public EntityManager getEntityManager()
    {
        return entityManager;
    }

    //can be used e.g. by a custom strategy for logging,...
    @SuppressWarnings("UnusedDeclaration")
    public Class<? extends Annotation> getQualifier()
    {
        return qualifier;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        EntityManagerEntry that = (EntityManagerEntry) o;

        if (!qualifier.equals(that.qualifier))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return qualifier.hashCode();
    }
}
