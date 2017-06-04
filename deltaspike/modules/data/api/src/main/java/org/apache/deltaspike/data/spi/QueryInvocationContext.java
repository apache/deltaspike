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
package org.apache.deltaspike.data.spi;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 * Expose the current query invocation to extensions.
 */
public interface QueryInvocationContext
{
    /**
     * Entity Manager used for the query.
     */
    EntityManager getEntityManager();

    /**
     * The class of the Entity related to the invoked Repository.
     */
    Class<?> getEntityClass();

    /**
     * Given the object parameter is an entity, checks if the entity is
     * persisted or not.
     *
     * @param entity Entity object, non nullable.
     * @return true if the entity is not persisted, false otherwise and if no entity.
     */
    boolean isNew(Object entity);

    /**
     * The type of the repository currently accessed.
     */
    Class<?> getRepositoryClass();

    /**
     * The repository method currently executed.
     */
    Method getMethod();
}
