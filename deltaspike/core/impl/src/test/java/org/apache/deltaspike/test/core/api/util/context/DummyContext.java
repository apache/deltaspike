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
package org.apache.deltaspike.test.core.api.util.context;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;

import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;

/**
 * Non passivating scoped test context
 */
public class DummyContext extends AbstractContext
{
    private boolean active = true;
    private boolean concurrent;
    private ContextualStorage storage = null;
    private BeanManager beanManager;

    public DummyContext(BeanManager beanManager, boolean concurrent)
    {
        super(beanManager);
        this.concurrent = concurrent;
        this.beanManager = beanManager;
    }

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExists)
    {
        if (storage == null && createIfNotExists)
        {
            storage = new ContextualStorage(beanManager, concurrent, isPassivatingScope());
        }

        return storage;
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return DummyScoped.class;
    }

    @Override
    public boolean isActive()
    {
        return active;
    }


    public void setActive(boolean active)
    {
        this.active = active;
    }
}
