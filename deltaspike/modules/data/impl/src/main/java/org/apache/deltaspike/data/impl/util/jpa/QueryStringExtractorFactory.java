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
package org.apache.deltaspike.data.impl.util.jpa;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Query;

public class QueryStringExtractorFactory
{

    private final List<QueryStringExtractor> extractors = Arrays.<QueryStringExtractor> asList(
            new HibernateQueryStringExtractor(),
            new EclipseLinkEjbQueryStringExtractor(),
            new OpenJpaQueryStringExtractor());

    public QueryStringExtractor select(Query query)
    {
        for (QueryStringExtractor extractor : extractors)
        {
            String compare = extractor.getClass().getAnnotation(ProviderSpecific.class).value();
            if (isQueryClass(compare, query))
            {
                return extractor;
            }
        }
        throw new RuntimeException("Persistence provider not supported");
    }

    private boolean isQueryClass(String clazzName, Query query)
    {
        try
        {
            Class<?> toClass = Class.forName(clazzName);
            toClass.cast(query);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

}
