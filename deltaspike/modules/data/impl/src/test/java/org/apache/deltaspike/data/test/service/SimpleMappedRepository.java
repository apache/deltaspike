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
package org.apache.deltaspike.data.test.service;

import java.util.List;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.mapping.MappingConfig;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.dto.BooleanWrapper;
import org.apache.deltaspike.data.test.domain.dto.SimpleDto;
import org.apache.deltaspike.data.test.domain.dto.SimpleId;

@Repository(forEntity = Simple.class)
@MappingConfig(SimpleMapper.class)
public interface SimpleMappedRepository extends EntityRepository<SimpleDto, SimpleId>
{

    SimpleDto findByName(String name);

    List<SimpleDto> findByEnabled(Boolean enabled);

    @MappingConfig(WrappedMapper.class)
    List<SimpleDto> findByEnabled(BooleanWrapper enabled);

    @Query("select e from Simple e where e.name = ?1")
    QueryResult<SimpleDto> findByNameToo(String name);

}
