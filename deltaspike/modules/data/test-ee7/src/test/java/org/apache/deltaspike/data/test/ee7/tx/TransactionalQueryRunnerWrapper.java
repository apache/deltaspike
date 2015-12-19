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
package org.apache.deltaspike.data.test.ee7.tx;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;

import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.tx.TransactionalQueryRunner;

@Specializes
@ApplicationScoped
public class TransactionalQueryRunnerWrapper extends TransactionalQueryRunner
{

    private boolean runInTx = false;
    private boolean runInNonTx = false;

    public void reset()
    {
        runInTx = false;
        runInNonTx = false;
    }

    @Override
    protected Object executeNonTransactional(QueryBuilder builder, CdiQueryInvocationContext context)
    {
        runInNonTx = true;
        return super.executeNonTransactional(builder, context);
    }

    @Override
    protected Object executeTransactional(QueryBuilder builder, CdiQueryInvocationContext context) throws Exception
    {
        runInTx = true;
        return super.executeTransactional(builder, context);
    }

    public boolean isRunInTx()
    {
        return runInTx;
    }

    public boolean isRunInNonTx()
    {
        return runInNonTx;
    }

}
