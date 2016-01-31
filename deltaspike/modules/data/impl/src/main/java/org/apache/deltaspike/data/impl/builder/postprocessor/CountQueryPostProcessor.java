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
package org.apache.deltaspike.data.impl.builder.postprocessor;

import static org.apache.deltaspike.core.util.StringUtils.isNotEmpty;
import static org.apache.deltaspike.data.impl.util.QueryUtils.nullSafeValue;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Query;

import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.handler.JpaQueryPostProcessor;
import org.apache.deltaspike.data.impl.param.Parameters;
import org.apache.deltaspike.data.impl.util.jpa.QueryStringExtractorFactory;

public class CountQueryPostProcessor implements JpaQueryPostProcessor
{

    private static final Logger log = Logger.getLogger(CountQueryPostProcessor.class.getName());

    private final QueryStringExtractorFactory factory = new QueryStringExtractorFactory();

    @Override
    public Query postProcess(CdiQueryInvocationContext context, Query query)
    {
        String queryString = getQueryString(context, query);
        QueryExtraction extract = new QueryExtraction(queryString);
        String count = extract.rewriteToCount();
        log.log(Level.FINER, "Rewrote query {0} to {1}", new Object[] { queryString, count });
        Query result = context.getEntityManager().createQuery(count);
        Parameters params = context.getParams();
        params.applyTo(result);
        return result;
    }

    private String getQueryString(CdiQueryInvocationContext context, Query query)
    {
        if (isNotEmpty(context.getQueryString()))
        {
            return context.getQueryString();
        }
        return factory.extract(query);
    }

    private static class QueryExtraction
    {

        private String select;
        private String from;
        private String where;

        private String entityName;
        private final String query;

        public QueryExtraction(String query)
        {
            this.query = query;
        }

        public String rewriteToCount()
        {
            splitQuery();
            extractEntityName();
            return rewrite();
        }

        private String rewrite()
        {
            return new StringBuilder()
                    .append("select count(")
                        .append(nullSafeValue(select, entityName))
                    .append(") ")
                    .append(from)
                    .append(nullSafeValue(where))
                    .toString();
        }

        private void extractEntityName()
        {
            String[] split = from.split(" ");
            if (split.length > 1)
            {
                entityName = split[split.length - 1];
            }
            else
            {
                entityName = "*";
            }
        }

        private void splitQuery()
        {
            String lower = query.toLowerCase();
            int selectIndex = lower.indexOf("select");
            int fromIndex = lower.indexOf("from");
            int whereIndex = lower.indexOf("where");
            int orderByIndex = lower.indexOf("order by");
            if (selectIndex >= 0)
            {
                select = query.substring("select".length(), fromIndex);
            }
            if (whereIndex >= 0)
            {
                from = query.substring(fromIndex, whereIndex);
                where = query.substring(whereIndex);
                if (orderByIndex > 0)
                {
                    where = where.substring(0, orderByIndex - whereIndex);
                }
            }
            else
            {
                from = query.substring(fromIndex);
            }
        }

    }

}
