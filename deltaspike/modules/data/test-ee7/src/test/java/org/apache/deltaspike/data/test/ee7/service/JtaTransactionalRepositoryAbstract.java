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
package org.apache.deltaspike.data.test.ee7.service;

import static javax.persistence.LockModeType.PESSIMISTIC_READ;

import jakarta.inject.Inject;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.SingleResultType;
import org.apache.deltaspike.data.test.ee7.domain.Simple;

@Repository
@javax.transaction.Transactional
public abstract class JtaTransactionalRepositoryAbstract 
    extends AbstractEntityRepository<Simple, Long>
{
    @Inject
    private SimpleHolderTx simpleHolderTx;

    @Inject
    private SimpleHolderApp simpleHolderApp;

    @Inject
    private SimpleHolderDep simpleHolderDep;

    @Query(lock = PESSIMISTIC_READ, singleResult = SingleResultType.OPTIONAL)
    public abstract Simple findOptionalByName(String name);

    @Modifying @Query("delete from Simple")
    public abstract int deleteAll();
    
    public Simple saveOnMatchTx(Simple simple)
    {
        if (simpleHolderTx.getSimple() == null)
        {
            return null;
        }
        
        if (simple.getName().equals(simpleHolderTx.getSimple().getName()))
        {
            return save(simple);
        }
        
        return null;
    }

    public Simple saveOnMatchApp(Simple simple)
    {
        if (simpleHolderApp.getSimple() == null)
        {
            return null;
        }
        
        if (simple.getName().equals(simpleHolderApp.getSimple().getName()))
        {
            return save(simple);
        }
        
        return null;
    }

    public Simple saveOnMatchDep(Simple simple)
    {
        if (simpleHolderDep.getSimple() == null)
        {
            return null;
        }
        
        if (simple.getName().equals(simpleHolderDep.getSimple().getName()))
        {
            return save(simple);
        }
        
        return null;
    }
}
