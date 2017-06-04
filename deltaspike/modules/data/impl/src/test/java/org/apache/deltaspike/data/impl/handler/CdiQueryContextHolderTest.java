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
package org.apache.deltaspike.data.impl.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodPrefix;
import org.apache.deltaspike.data.impl.meta.EntityMetadata;
import org.apache.deltaspike.data.impl.meta.RepositoryMetadata;

import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.service.SimpleRepository;
import org.junit.Test;

public class CdiQueryContextHolderTest
{

    @Test
    public void should_dispose_tl_when_all_empty()
    {
        // given
        CdiQueryContextHolder holder = new CdiQueryContextHolder();
        CdiQueryInvocationContext context = dummyInvocationContext();

        // when
        holder.set(context);
        CdiQueryInvocationContext temp1 = holder.get();
        CdiQueryInvocationContext temp2 = holder.get();
        holder.dispose();

        // then
        assertEquals(context, temp1);
        assertEquals(context, temp2);
        assertNull(holder.get());
    }

    @Test
    public void should_not_dispose_when_stack_not_empty()
    {
        // given
        CdiQueryContextHolder holder = new CdiQueryContextHolder();
        CdiQueryInvocationContext context1 = dummyInvocationContext();
        CdiQueryInvocationContext context2 = dummyInvocationContext();

        // when
        holder.set(context1);
        holder.set(context2);
        CdiQueryInvocationContext temp1 = holder.get();
        holder.dispose();
        CdiQueryInvocationContext temp2 = holder.get();
        holder.dispose();

        // then
        assertEquals(context2, temp1);
        assertEquals(context1, temp2);
        assertNull(holder.get());
    }

    private CdiQueryInvocationContext dummyInvocationContext()
    {
        return new CdiQueryInvocationContext(null, dummyMethod(), null, dummyRepo(), dummyRepoMethod(dummyRepo()), null);
    }

    private Method dummyMethod()
    {
        try
        {
            return SimpleRepository.class.getMethod("findAnyByName", String.class);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private RepositoryMethodMetadata dummyRepoMethod(RepositoryMetadata metadata)
    {
        RepositoryMethodMetadata methodMetadata = new RepositoryMethodMetadata(dummyMethod());
        methodMetadata.setMethodPrefix(new RepositoryMethodPrefix(
                    metadata.getRepositoryClass().getAnnotation(Repository.class).methodPrefix(),
                    dummyMethod().getName()));
        
        return methodMetadata;
    }

    private RepositoryMetadata dummyRepo()
    {
        return new RepositoryMetadata(SimpleRepository.class, new EntityMetadata(Simple.class));
    }
}
