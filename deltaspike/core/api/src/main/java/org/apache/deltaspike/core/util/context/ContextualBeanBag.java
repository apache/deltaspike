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

package org.apache.deltaspike.core.util.context;


import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 * This data holder contains all necessary data you need to
 * store a Contextual Instance in a CDI Context.
 *
 * This class is intended for use in Contexts for non-passivating Scopes only!
 */
public class ContextualBeanBag
{
    /**
     * The actual Contextual Instance in the context
     */
    protected Object contextualInstance;

    /**
     * We need to store the CreationalContext as we need it for
     * properly destroying the contextual instance via
     * {@link Contextual#destroy(Object, javax.enterprise.context.spi.CreationalContext)}
     */
    protected CreationalContext<?> creationalContext;

    /**
     * The Bean for the contextual instance.
     * This is not guaranteed to be Serializable in CDI-1.0!.
     */
    protected transient Contextual<?> bean;

    public ContextualBeanBag(Object contextualInstance, CreationalContext<?> creationalContext, Contextual<?> bean)
    {
        this.contextualInstance = contextualInstance;
        this.creationalContext = creationalContext;
        this.bean = bean;
    }


    public Contextual<?> getBean()
    {
        return bean;
    }

    public Object getContextualInstance()
    {
        return contextualInstance;
    }

    public CreationalContext<?> getCreationalContext()
    {
        return creationalContext;
    }
}
