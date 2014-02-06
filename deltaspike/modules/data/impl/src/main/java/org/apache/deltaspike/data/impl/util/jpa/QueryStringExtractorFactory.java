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

import javax.persistence.Query;

public class QueryStringExtractorFactory
{

    private final QueryStringExtractor[] extractors = new QueryStringExtractor[]
    {
        new HibernateQueryStringExtractor(),
        new EclipseLinkEjbQueryStringExtractor(),
        new OpenJpaQueryStringExtractor()
    };

    public String extract(final Query query)
    {
        for (final QueryStringExtractor extractor : extractors)
        {
            final String compare = extractor.getClass().getAnnotation(ProviderSpecific.class).value();
            final Object implQuery = toImplQuery(compare, query);
            if (implQuery != null)
            {
                return extractor.extractFrom(implQuery);
            }
        }
        throw new RuntimeException("Persistence provider not supported");
    }

    private static Object toImplQuery(final String clazzName, final Query query)
    {
        try
        {
            Class<?> toClass = Class.forName(clazzName);
            try
            {
                // throw a persistence exception if not possible
                return query.unwrap(toClass);
            }
            catch (Exception e)
            {
                toClass.cast(query);
                return query;
            }
        }
        catch (Exception e)
        {
            return null;
        }
    }

}
