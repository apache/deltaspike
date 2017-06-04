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
package org.apache.deltaspike.data.impl.tx;

import java.lang.reflect.Method;
import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;
import javax.persistence.LockModeType;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;

import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.handler.EntityRepositoryHandler;
import org.apache.deltaspike.data.impl.handler.QueryRunner;
import org.apache.deltaspike.data.impl.meta.RequiresTransaction;
import org.apache.deltaspike.jpa.spi.entitymanager.ActiveEntityManagerHolder;
import org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy;

@ApplicationScoped
public class TransactionalQueryRunner implements QueryRunner
{

    @Inject
    private TransactionStrategy strategy;

    @Inject
    private ActiveEntityManagerHolder activeEntityManagerHolder;

    @Override
    public Object executeQuery(final QueryBuilder builder, final CdiQueryInvocationContext context)
        throws Throwable
    {
        if (needsTransaction(context))
        {
            try
            {
                activeEntityManagerHolder.set(context.getEntityManager());
                return executeTransactional(builder, context);
            }
            finally
            {
                activeEntityManagerHolder.dispose();
            }
        }
        return executeNonTransactional(builder, context);
    }

    protected Object executeNonTransactional(final QueryBuilder builder, final CdiQueryInvocationContext context)
    {
        return builder.executeQuery(context);
    }

    protected Object executeTransactional(final QueryBuilder builder, final CdiQueryInvocationContext context)
        throws Exception
    {
        return strategy.execute(new InvocationContextWrapper(context)
        {
            @Override
            public Object proceed() throws Exception
            {
                return builder.executeQuery(context);
            }
        });
    }

    private boolean needsTransaction(CdiQueryInvocationContext context)
    {
        if (ClassUtils.containsMethod(EntityRepositoryHandler.class, context.getMethod()))
        {
            Method executed = ClassUtils.extractMethod(EntityRepositoryHandler.class, context.getMethod());
            if (executed.isAnnotationPresent(RequiresTransaction.class))
            {
                return true;
            }
        }

        Query query = context.getRepositoryMethodMetadata().getQuery();
        Modifying modifying = context.getRepositoryMethodMetadata().getModifying();
        
        if ((query != null && !query.lock().equals(LockModeType.NONE)) || modifying != null)
        {
            return true;
        }
        
        return false;
    }

}
