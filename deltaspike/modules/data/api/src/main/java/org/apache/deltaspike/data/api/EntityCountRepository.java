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

import javax.persistence.metamodel.SingularAttribute;

public interface EntityCountRepository<E>
{
    /**
     * Count all existing entities of entity class {@code <E>}.
     * @return                  Counter.
     */
    Long count();

    /**
     * Count existing entities of entity class {@code <E>}
     * with for a given object and a specific set of properties..
     * @param example           Sample entity. Query all like.
     * @param attributes        Which attributes to consider for the query.
     *
     * @return                  Counter.
     */
    Long count(E example, SingularAttribute<E, ?>... attributes);

    /**
     * Count existing entities of entity class using the like operator for String attributes {@code <E>}
     * with for a given object and a specific set of properties..
     * @param example           Sample entity. Query all like.
     * @param attributes        Which attributes to consider for the query.
     *
     * @return                  Counter.
     */
    Long countLike(E example, SingularAttribute<E, ?>... attributes);
}
