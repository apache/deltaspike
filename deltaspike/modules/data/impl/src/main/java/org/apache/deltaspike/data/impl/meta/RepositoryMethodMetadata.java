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
package org.apache.deltaspike.data.impl.meta;

import java.lang.reflect.Method;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.SingleResultType;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.builder.part.QueryRoot;
import org.apache.deltaspike.data.impl.builder.result.QueryProcessor;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

public class RepositoryMethodMetadata
{
    private Method method;
    private RepositoryMethodType methodType;
    private RepositoryMethodPrefix methodPrefix;
    
    private Query query;
    private Modifying modifying;
    
    private QueryRoot queryRoot;
    private QueryProcessor queryProcessor;

    private Class<? extends QueryInOutMapper<?>> queryInOutMapperClass;
    private boolean queryInOutMapperIsNormalScope;
    
    private boolean returnsOptional;
    private boolean returnsStream;
    
    private SingleResultType singleResultType;
    
    private boolean requiresTransaction;
    
    private Transactional transactional;

    public RepositoryMethodMetadata()
    {
        
    }
    
    public RepositoryMethodMetadata(Method method)
    {
        this.method = method;
    }
    
    public Method getMethod()
    {
        return method;
    }

    public void setMethod(Method method)
    {
        this.method = method;
    }

    public RepositoryMethodType getMethodType()
    {
        return methodType;
    }

    public void setMethodType(RepositoryMethodType methodType)
    {
        this.methodType = methodType;
    }

    public RepositoryMethodPrefix getMethodPrefix()
    {
        return methodPrefix;
    }

    public void setMethodPrefix(RepositoryMethodPrefix methodPrefix)
    {
        this.methodPrefix = methodPrefix;
    }

    public QueryRoot getQueryRoot()
    {
        return queryRoot;
    }

    public void setQueryRoot(QueryRoot queryRoot)
    {
        this.queryRoot = queryRoot;
    }

    public QueryProcessor getQueryProcessor()
    {
        return queryProcessor;
    }

    public void setQueryProcessor(QueryProcessor queryProcessor)
    {
        this.queryProcessor = queryProcessor;
    }

    public Class<? extends QueryInOutMapper<?>> getQueryInOutMapperClass()
    {
        return queryInOutMapperClass;
    }

    public void setQueryInOutMapperClass(Class<? extends QueryInOutMapper<?>> queryInOutMapperClass)
    {
        this.queryInOutMapperClass = queryInOutMapperClass;
    }

    public boolean isQueryInOutMapperIsNormalScope()
    {
        return queryInOutMapperIsNormalScope;
    }

    public void setQueryInOutMapperIsNormalScope(boolean queryInOutMapperIsNormalScope)
    {
        this.queryInOutMapperIsNormalScope = queryInOutMapperIsNormalScope;
    }

    public Query getQuery()
    {
        return query;
    }

    public void setQuery(Query query)
    {
        this.query = query;
    }

    public Modifying getModifying() 
    {
        return modifying;
    }

    public void setModifying(Modifying modifying)
    {
        this.modifying = modifying;
    } 

    public boolean isReturnsOptional()
    {
        return returnsOptional;
    }

    public void setReturnsOptional(boolean returnsOptional)
    {
        this.returnsOptional = returnsOptional;
    }

    public boolean isReturnsStream()
    {
        return returnsStream;
    }

    public void setReturnsStream(boolean returnsStream)
    {
        this.returnsStream = returnsStream;
    }

    public SingleResultType getSingleResultType()
    {
        return singleResultType;
    }

    public void setSingleResultType(SingleResultType singleResultType)
    {
        this.singleResultType = singleResultType;
    }

    public boolean isRequiresTransaction()
    {
        return requiresTransaction;
    }

    public void setRequiresTransaction(boolean requiresTransaction)
    {
        this.requiresTransaction = requiresTransaction;
    }

    public Transactional getTransactional()
    {
        return transactional;
    }

    public void setTransactional(Transactional transactional)
    {
        this.transactional = transactional;
    }
}
