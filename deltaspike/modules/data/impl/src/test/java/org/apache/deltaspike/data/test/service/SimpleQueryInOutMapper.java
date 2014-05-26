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

import org.apache.deltaspike.data.api.mapping.SimpleQueryInOutMapperBase;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.dto.SimpleDto;
import org.apache.deltaspike.data.test.domain.dto.SimpleId;

public class SimpleQueryInOutMapper extends SimpleQueryInOutMapperBase<Simple, SimpleDto>
{

    @Override
    protected Object getPrimaryKey(SimpleDto dto)
    {
        return dto.getId() != null ? dto.getId().getId() : null;
    }

    @Override
    protected SimpleDto toDto(Simple entity)
    {
        SimpleDto dto = new SimpleDto();
        dto.setId(new SimpleId(entity.getId()));
        dto.setEnabled(entity.getEnabled());
        dto.setName(entity.getName());
        return dto;
    }

    @Override
    protected Simple toEntity(Simple entity, SimpleDto dto)
    {
        entity.setName(dto.getName());
        entity.setEnabled(dto.getEnabled());
        return entity;
    }

}
