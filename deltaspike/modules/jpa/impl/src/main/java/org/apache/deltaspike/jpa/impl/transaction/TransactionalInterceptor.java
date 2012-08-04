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
package org.apache.deltaspike.jpa.impl.transaction;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

/**
 * Interceptor for wrapping transactional database requests.
 * This interceptor itself doesn't contain any functionality.
 * Instead the 'real' work is done inside a pluggable
 * {@link org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy}.
 */
@Interceptor
@Transactional
public class TransactionalInterceptor implements Serializable
{
    private static final long serialVersionUID = 8787285444722371172L;

    @Inject
    private TransactionStrategy transactionStrategy;

    /**
     * Creates a transaction before the intercepted method gets called and commits or reverts it after the invocation.
     * A {@link org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy}
     * is allowed to begin the transaction lazily but it has to support nested interceptor calls.
     *
     * @param invocationContext current invocation-context
     * @return result of the intercepted method
     * @throws Exception exception which might be thrown by the intercepted method
     */
    @AroundInvoke
    public Object executeInTransaction(InvocationContext invocationContext) throws Exception
    {
        return transactionStrategy.execute(invocationContext);
    }
}
