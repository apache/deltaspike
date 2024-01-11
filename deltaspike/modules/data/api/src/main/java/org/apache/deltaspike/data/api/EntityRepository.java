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

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.metamodel.SingularAttribute;

/**
 * Base Repository interface. All methods are implemented by the CDI extension.
 *
 * @param <E>   Entity type.
 * @param <PK>  Primary key type.
 */
public interface EntityRepository<E, PK extends Serializable> extends EntityPersistenceRepository<E, PK>,
        EntityCountRepository<E>
{

    /**
     * Entity lookup by primary key. Convenicence method around
     * {@link jakarta.persistence.EntityManager#find(Class, Object)}.
     * @param primaryKey        DB primary key.
     * @return                  Entity identified by primary or null if it does not exist.
     */
    E findBy(PK primaryKey);
    
    /**
     * Entity lookup by primary key. Convenicence method around
     * {@link jakarta.persistence.EntityManager#find(Class, Object)}.
     * @param primaryKey        DB primary key.
     * @return                  Entity identified by primary or null if it does not exist, wrapped by Optional.
     */
    Optional<E> findOptionalBy(PK primaryKey);

    /**
     * Lookup all existing entities of entity class {@code <E>}.
     * @return                  List of entities, empty if none found.
     */
    List<E> findAll();

    /**
     * Lookup a range of existing entities of entity class {@code <E>} with support for pagination.
     * @param start             The starting position.
     * @param max               The maximum number of results to return
     * @return                  List of entities, empty if none found.
     */
    List<E> findAll(int start, int max);

    /**
     * Query by example - for a given object and a specific set of properties.
     * @param example           Sample entity. Query all like.
     * @param attributes        Which attributes to consider for the query.
     * @return                  List of entities matching the example, or empty if none found.
     */
    List<E> findBy(E example, SingularAttribute<E, ?>... attributes);

    /**
     * Query by example - for a given object and a specific set of properties with support for pagination.
     * @param example           Sample entity. Query all like.
     * @param start             The starting position.
     * @param max               The maximum number of results to return
     * @param attributes        Which attributes to consider for the query.
     * @return                  List of entities matching the example, or empty if none found.
     */
    List<E> findBy(E example, int start, int max, SingularAttribute<E, ?>... attributes);

    /**
     * Query by example - for a given object and a specific set of properties using a like operator for Strings.
     * @param example           Sample entity. Query all like.
     * @param attributes        Which attributes to consider for the query.
     * @return                  List of entities matching the example, or empty if none found.
     */
    List<E> findByLike(E example, SingularAttribute<E, ?>... attributes);

    /**
     * Query by example - for a given object and a specific set of properties
     * using a like operator for Strings with support for pagination.
     * @param example           Sample entity. Query all like.
     * @param start             The starting position.
     * @param max               The maximum number of results to return
     * @param attributes        Which attributes to consider for the query.
     * @return                  List of entities matching the example, or empty if none found.
     */
    List<E> findByLike(E example, int start, int max, SingularAttribute<E, ?>... attributes);
}
