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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.deltaspike.core.util.ClassUtils;

public final class EntityGraphHelper
{

    private static final Class<?> ENTITY_GRAPH_CLASS;
    private static final Class<?> SUBGRAPH_CLASS;
    private static final Method ADD_ATTRIBUTE_NODES;
    private static final Method ADD_SUBGRAPH;
    private static final Method SUBGRAPH_ADD_ATTRIBUTE_NODES;

    static
    {
        ENTITY_GRAPH_CLASS = ClassUtils.tryToLoadClassForName("javax.persistence.EntityGraph");
        SUBGRAPH_CLASS = ClassUtils.tryToLoadClassForName("javax.persistence.Subgraph");
        if (ENTITY_GRAPH_CLASS == null)
        {
            ADD_ATTRIBUTE_NODES = null;
            ADD_SUBGRAPH = null;
            SUBGRAPH_ADD_ATTRIBUTE_NODES = null;
        }
        else
        {
            try
            {
                ADD_ATTRIBUTE_NODES = ENTITY_GRAPH_CLASS.getMethod("addAttributeNodes",
                    String[].class);
                ADD_SUBGRAPH = ENTITY_GRAPH_CLASS.getMethod("addSubgraph", String.class);
                SUBGRAPH_ADD_ATTRIBUTE_NODES = SUBGRAPH_CLASS.getMethod("addAttributeNodes",
                    String[].class);
            }
            catch (NoSuchMethodException e)
            {
                throw new EntityGraphException(e.getMessage(), e.getCause());
            }
            catch (SecurityException e)
            {
                throw new EntityGraphException(e.getMessage(), e.getCause());
            }
        }
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

    public static Object createEntityGraph(EntityManager em, Class<?> entityClass)
    {
        ensureAvailable();
        try
        {
            Method method = EntityManager.class.getMethod("createEntityGraph", Class.class);
            return method.invoke(em, entityClass);
        }
        catch (NoSuchMethodException e)
        {
            throw new EntityGraphException("no method EntityManager.createEntityGraph()", e);
        }
        catch (SecurityException e)
        {
            throw new EntityGraphException("no access to method EntityManager.createEntityGraph()",
                e);
        }
        catch (IllegalAccessException e)
        {
            throw new EntityGraphException("no access to method EntityManager.createEntityGraph()",
                e);
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
            throw new EntityGraphException("Class java.persistence.EntityGraph is not available. "
                + "Does your PersistenceProvider support JPA 2.1?");
        }
    }

    public static Object addSubgraph(Object entityGraph, String attributeName)
    {
        try
        {
            return ADD_SUBGRAPH.invoke(entityGraph, attributeName);
        }
        catch (IllegalAccessException e)
        {
            throw new EntityGraphException("no access to method EntityGraph.addSubgraph()", e);
        }
        catch (InvocationTargetException e)
        {
            throw new EntityGraphException(e.getCause().getMessage(), e.getCause());
        }
    }

    public static void addAttributeNodes(Object graph, String attributeName)
    {
        try
        {
            if (ENTITY_GRAPH_CLASS.isInstance(graph))
            {
                ADD_ATTRIBUTE_NODES.invoke(graph, new Object[] { new String[] { attributeName } });
            }
            else if (SUBGRAPH_CLASS.isInstance(graph))
            {
                SUBGRAPH_ADD_ATTRIBUTE_NODES.invoke(graph,
                    new Object[] { new String[] { attributeName } });
            }
        }
        catch (IllegalAccessException e)
        {
            throw new EntityGraphException("no access to method addAttributeNodes()", e);
        }
        catch (InvocationTargetException e)
        {
            throw new EntityGraphException(e.getCause().getMessage(), e.getCause());
        }
    }

    public static Object buildEntityGraph(EntityManager em, Class<?> entityClass,
        String[] attributePaths)
    {
        Object graph = createEntityGraph(em, entityClass);
        List<String> paths = new ArrayList<String>(Arrays.asList(attributePaths));

        Collections.sort(paths);
        Collections.reverse(paths);

        for (String path : attributePaths)
        {
            if (path.contains("."))
            {
                String[] segments = path.split("\\.");
                Object parent = addSubgraph(graph, segments[0]);

                for (int i = 1; i < segments.length - 1; i++)
                {
                    addSubgraph(parent, segments[i]);
                }

                addAttributeNodes(parent, segments[segments.length - 1]);
            }
            else
            {
                addAttributeNodes(graph, path);
            }
        }
        return graph;
    }
}
