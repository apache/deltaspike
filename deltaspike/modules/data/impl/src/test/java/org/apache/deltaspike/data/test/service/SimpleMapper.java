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

import java.util.ArrayList;
import java.util.List;

import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.dto.SimpleDto;
import org.apache.deltaspike.data.test.domain.dto.SimpleId;

public class SimpleMapper implements QueryInOutMapper<Simple>
{

    @Override
    public Object mapResult(Simple result)
    {
        SimpleDto dto = new SimpleDto();
        dto.setId(new SimpleId(result.getId()));
        dto.setName(result.getName());
        dto.setEnabled(result.getEnabled());
        return dto;
    }

    @Override
    public Object mapResultList(List<Simple> result)
    {
        List<SimpleDto> dtos = new ArrayList<SimpleDto>(result.size());
        for (Simple simple : result)
        {
            dtos.add((SimpleDto) mapResult(simple));
        }
        return dtos;
    }

    @Override
    public boolean mapsParameter(Object parameter)
    {
        return parameter != null && (
                parameter instanceof SimpleDto || parameter instanceof SimpleId);
    }

    @Override
    public Object mapParameter(Object parameter)
    {
        if (parameter instanceof SimpleDto)
        {
            SimpleDto dto = (SimpleDto) parameter;
            Simple simple = new Simple(dto.getName());
            simple.setId(dto.getId() != null ? dto.getId().getId() : null);
            simple.setEnabled(dto.getEnabled());
            return simple;
        }
        return ((SimpleId) parameter).getId();
    }

}
