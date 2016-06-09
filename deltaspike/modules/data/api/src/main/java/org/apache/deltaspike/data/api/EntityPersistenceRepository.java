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

package org.apache.deltaspike.data.api;

import org.apache.deltaspike.core.spi.activation.Deactivatable;

import java.io.Serializable;

public interface EntityPersistenceRepository<E, PK extends Serializable> extends Deactivatable
{
    /**
     * Persist (new entity) or merge the given entity. The distinction on calling either
     * method is done based on the primary key field being null or not.
     * If this results in wrong behavior for a specific case, consider using the
     * {@link org.apache.deltaspike.data.api.EntityManagerDelegate} which offers both
     * {@code persist} and {@code merge}.
     * @param entity            Entity to save.
     * @return                  Returns the modified entity.
     */
    E save(E entity);

    /**
     * {@link #save(Object)}s the given entity and flushes the persistence context afterwards.
     * @param entity            Entity to save.
     * @return                  Returns the modified entity.
     */
    E saveAndFlush(E entity);

    /**
     * {@link #save(Object)}s the given entity and flushes the persistence context afterwards,
     * followed by a refresh (e.g. to load DB trigger modifications).
     * @param entity            Entity to save.
     * @return                  Returns the modified entity.
     */
    E saveAndFlushAndRefresh(E entity);

    /**
     * Convenience access to {@link javax.persistence.EntityManager#remove(Object)}.
     * @param entity            Entity to remove.
     */
    void remove(E entity);

    /**
     * Convenience access to {@link javax.persistence.EntityManager#remove(Object)}
     * with a following flush.
     * @param entity            Entity to remove.
     */
    void removeAndFlush(E entity);

    /**
     * Convenience access to {@link javax.persistence.EntityManager#remove(Object)}
     * with an detached entity.
     * @param entity            Entity to remove.
     */
    void attachAndRemove(E entity);

    /**
     * Convenience access to {@link javax.persistence.EntityManager#refresh(Object)}.
     * @param entity            Entity to refresh.
     */
    void refresh(E entity);

    /**
     * Convenience access to {@link javax.persistence.EntityManager#flush()}.
     */
    void flush();

    /**
     * Return the id of the entity. Returns null if the entity does not yet have an id.
     * @param example           Sample entity.
     * @return                  id of the entity
     */
    PK getPrimaryKey(E example);
}
