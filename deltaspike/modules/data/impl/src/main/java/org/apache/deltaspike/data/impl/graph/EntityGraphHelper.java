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
package org.apache.deltaspike.data.impl.graph;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.apache.deltaspike.core.util.ClassUtils;

public final class EntityGraphHelper
{

    private static final Class<?> ENTITY_GRAPH_CLASS;

    static
    {
        ENTITY_GRAPH_CLASS = ClassUtils.tryToLoadClassForName("javax.persistence.EntityGraph");
    }
    
    
    private EntityGraphHelper()
    {
        // hidden constructor
    }

    public static boolean isAvailable()
    {
        return ENTITY_GRAPH_CLASS != null;
    }

    public static Object getEntityGraph(EntityManager em, String graphName)
    {
        ensureAvailable();
        try
        {
            Method method = EntityManager.class.getMethod("getEntityGraph", String.class);
            return method.invoke(em, graphName);
        }
        catch (NoSuchMethodException e)
        {
            throw new EntityGraphException("no method EntityManager.getEntityGraph()", e);
        }
        catch (SecurityException e)
        {
            throw new EntityGraphException("no access to method EntityManager.getEntityGraph()", e);
        }
        catch (IllegalAccessException e)
        {
            throw new EntityGraphException("no access to method EntityManager.getEntityGraph()", e);
        }
        catch (InvocationTargetException e)
        {
            throw new EntityGraphException(e.getCause().getMessage(), e.getCause());
        }
    }

    private static void ensureAvailable()
    {
        if (!isAvailable())
        {
            throw new EntityGraphException(
                "Class java.persistence.EntityGraph is not available. "
                + "Does your PersistenceProvider support JPA 2.1?");
        }
    }
}
