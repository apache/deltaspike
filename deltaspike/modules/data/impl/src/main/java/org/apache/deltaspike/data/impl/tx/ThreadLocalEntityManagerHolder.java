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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.persistence.EntityManager;

import org.apache.deltaspike.jpa.impl.entitymanager.DefaultEntityManagerHolder;

@Specializes
@ApplicationScoped
public class ThreadLocalEntityManagerHolder extends DefaultEntityManagerHolder
{

    private static final long serialVersionUID = 1L;

    private final ThreadLocal<EntityManager> holder = new ThreadLocal<EntityManager>();

    @Override
    public void set(EntityManager entityManager)
    {
        holder.set(entityManager);
    }

    @Override
    public boolean isSet()
    {
        return get() != null;
    }

    @Override
    public EntityManager get()
    {
        return holder.get();
    }

    @Override
    public void dispose()
    {
        holder.remove();
    }

}
