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
package org.apache.deltaspike.test.jpa.api.transactional.defaultinjection;

import org.apache.deltaspike.test.jpa.api.shared.TestEntityManager;
import org.apache.deltaspike.test.jpa.api.shared.TestException;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

@RequestScoped
public class TestEntityManagerProducer
{
    private TestEntityManager defaultEntityManager = new TestEntityManager();
    private TestEntityManager failedFlushEntityManager = new TestEntityManager() {
        @Override
        public void flush()
        {
            throw new TestException();
        }
    };

    @Produces
    protected EntityManager defaultEntityManager()
    {
        return defaultEntityManager;
    }

    @Produces
    @Failed
    protected EntityManager failedFlushEntityManager()
    {
        return failedFlushEntityManager;
    }

    public TestEntityManager getDefaultEntityManager()
    {
        return defaultEntityManager;
    }

    public TestEntityManager getFailedFlushEntityManager()
    {
        return failedFlushEntityManager;
    }
}
