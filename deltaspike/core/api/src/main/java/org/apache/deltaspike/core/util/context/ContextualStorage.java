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
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This Storage holds all information needed for storing
 * Contextual Instances in a Context.
 *
 * It also addresses Serialisation in case of passivating scopes.
 */
public class ContextualStorage implements Serializable
{
    private static final long serialVersionUID = 1L;

    private transient Map<Contextual<?>, ContextualInstanceInfo<?>> contextualInstances;

    private BeanManager beanManager;

    private boolean concurrent;

    public ContextualStorage(BeanManager beanManager, boolean concurrent)
    {
        this.beanManager = beanManager;
        this.concurrent = concurrent;
        if (concurrent)
        {
            contextualInstances = new ConcurrentHashMap<Contextual<?>, ContextualInstanceInfo<?>>();
        }
        else
        {
            contextualInstances = new HashMap<Contextual<?>, ContextualInstanceInfo<?>>();
        }
    }

    public Map<Contextual<?>, ContextualInstanceInfo<?>> getStorage()
    {
        return contextualInstances;
    }

    /**
     * Write the whole map to the stream.
     * The beans will be stored via it's passivationId as it is
     * not guaranteed that a Bean is Serializable in CDI-1.0.
     */
    private void writeObject(ObjectOutputStream s) throws IOException
    {
        s.writeLong(serialVersionUID);
        s.writeObject(beanManager);
        s.writeBoolean(concurrent);


        HashMap<String, ContextualInstanceInfo> serializableContextualInstances
            = new HashMap<String, ContextualInstanceInfo>(contextualInstances.size());

        String passivationId;

        for (Map.Entry<Contextual<?>, ContextualInstanceInfo<?>> entry : contextualInstances.entrySet())
        {
            Contextual<?> bean = entry.getKey();
            if (bean instanceof PassivationCapable)
            {
                passivationId = ((PassivationCapable) bean).getId();
            }
            else
            {
                throw new NotSerializableException("Could not serialize bean because it's not PassivationCapable");
            }

            serializableContextualInstances.put(passivationId, entry.getValue());
        }

        s.writeObject(serializableContextualInstances);
    }

    /**
     * Restore the bean info from the stream.
     * The passivationIds will translated back to Beans
     */
    private  void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException
    {
        if (s.readLong() == serialVersionUID)
        {
            beanManager = (BeanManager) s.readObject();
            concurrent = s.readBoolean();

            if (concurrent)
            {
                contextualInstances = new ConcurrentHashMap<Contextual<?>, ContextualInstanceInfo<?>>();
            }
            else
            {
                contextualInstances = new HashMap<Contextual<?>, ContextualInstanceInfo<?>>();
            }

            HashMap<String, ContextualInstanceInfo<?>> serializableContextualInstances
                = (HashMap<String, ContextualInstanceInfo<?>>) s.readObject();

            for (Map.Entry<String, ContextualInstanceInfo<?>> entry : serializableContextualInstances.entrySet())
            {
                Contextual<?> bean = beanManager.getPassivationCapableBean(entry.getKey());
                contextualInstances.put(bean, entry.getValue());
            }
        }
        else
        {
            throw new NotSerializableException("Could not deserialize due to wrong serialVersionUID");
        }
    }

}
