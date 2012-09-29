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
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A Serializable version of the ContextualBeanBag.
 * This class is used for Contexts managing passivating Beans.
 */
public class SerializableContextualBeanBag extends ContextualBeanBag implements Serializable
{
    /**Serial id*/
    private static final long serialVersionUID = 1L;

    /**
     * During Serialisation, then bean will be referenced via it's passivationId
     */
    private String passivatingBeanId = null;

    public SerializableContextualBeanBag(Object contextualInstance,
                                         CreationalContext<?> creationalContext,
                                         Contextual<?> bean)
    {
        super(contextualInstance, creationalContext, bean);
    }

    @Override
    public Contextual<?> getBean()
    {
        throw new IllegalStateException("Please use getBean with the BeanManager parameter instead!");
    }

    public Contextual<?> getBean(BeanManager beanManager)
    {
        if (bean != null)
        {
            return bean;
        }
        if (passivatingBeanId != null)
        {
            bean = beanManager.getPassivationCapableBean(passivatingBeanId);
            return bean;
        }
        throw new IllegalStateException("Neither the bean nor it's passivationId could be found!");
    }

    /**
     * Write to stream.
     * @param s stream
     * @throws java.io.IOException
     */
    private void writeObject(ObjectOutputStream s) throws IOException
    {
        s.writeLong(serialVersionUID);

        String passivationId = null;
        if (bean instanceof PassivationCapable)
        {
            passivationId = ((PassivationCapable) bean).getId();
        }

        if (passivationId != null)
        {
            s.writeObject(passivationId);
            s.writeObject(creationalContext);
            s.writeObject(contextualInstance);
        }
        else
        {
            throw new NotSerializableException("Could not serialize bean because it's not PassivationCapable");
        }
    }

    /**
     * Read from stream.
     * @param s stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private  void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException
    {
        if (s.readLong() == serialVersionUID)
        {
            String passivationId = (String) s.readObject();
            creationalContext = (CreationalContext<?>) s.readObject();
            contextualInstance = s.readObject();
        }
        else
        {
            throw new NotSerializableException("Could not deserialize due to wrong serialVersionUID");
        }
    }

}

