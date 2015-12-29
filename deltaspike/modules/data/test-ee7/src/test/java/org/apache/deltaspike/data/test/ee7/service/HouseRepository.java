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
package org.apache.deltaspike.data.test.ee7.service;

import org.apache.deltaspike.data.api.EntityGraph;
import org.apache.deltaspike.data.api.FullEntityRepository;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.test.ee7.domain.House;

@Repository
@org.apache.deltaspike.jpa.api.transaction.Transactional
public interface HouseRepository extends FullEntityRepository<House, Long>
{
    @EntityGraph("withFlats")
    House findByName(String name);

    House findOptionalByName(String name);
    
    @Query("select h from House h where h.name = ?1")
    @EntityGraph("withFlats")
    House fetchByName(String name);

    @Query("select h from House h where h.name = ?1")
    @EntityGraph("none")
    House fetchByNameWithInvalidGraph(String name);

    @Modifying @Query("delete from House")
    int deleteAll();

}
