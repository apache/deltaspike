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
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.data.api.EntityGraph;

/**
 * Helper for invoking methods related to entity graphs by reflection.
 * <p>
 * Reflection is required to stay compatible with JPA 2.0.
 */
public final class EntityGraphHelper
{

    private static final Class<?> ENTITY_GRAPH_CLASS;
    private static final Class<?> SUBGRAPH_CLASS;
    private static final Method EG_ADD_ATTRIBUTE_NODES;
    private static final Method EG_ADD_SUBGRAPH;
    private static final Method SUBGRAPH_ADD_ATTRIBUTE_NODES;
    private static final Method SUBGRAPH_ADD_SUBGRAPH;
    private static final Method EM_GET_ENTITY_GRAPH;
    private static final Method EM_CREATE_ENTITY_GRAPH;

    static
    {
        ENTITY_GRAPH_CLASS = ClassUtils.tryToLoadClassForName("javax.persistence.EntityGraph");
        SUBGRAPH_CLASS = ClassUtils.tryToLoadClassForName("javax.persistence.Subgraph");
        if (ENTITY_GRAPH_CLASS == null)
        {
            EG_ADD_ATTRIBUTE_NODES = null;
            EG_ADD_SUBGRAPH = null;
            SUBGRAPH_ADD_ATTRIBUTE_NODES = null;
            SUBGRAPH_ADD_SUBGRAPH = null;
            EM_GET_ENTITY_GRAPH = null;
            EM_CREATE_ENTITY_GRAPH = null;
        }
        else
        {
            try
            {
                EG_ADD_ATTRIBUTE_NODES = ENTITY_GRAPH_CLASS.getMethod("addAttributeNodes",
                    String[].class);
                EG_ADD_SUBGRAPH = ENTITY_GRAPH_CLASS.getMethod("addSubgraph", String.class);
                SUBGRAPH_ADD_ATTRIBUTE_NODES = SUBGRAPH_CLASS.getMethod("addAttributeNodes",
                    String[].class);
                SUBGRAPH_ADD_SUBGRAPH = SUBGRAPH_CLASS.getMethod("addSubgraph", String.class);
                EM_GET_ENTITY_GRAPH = EntityManager.class.getMethod("getEntityGraph", String.class);
                EM_CREATE_ENTITY_GRAPH = EntityManager.class.getMethod("createEntityGraph",
                    Class.class);
            }
            catch (NoSuchMethodException e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
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

    /*
     * TODO Check if this can be replaced by org.apache.deltaspike.core.util.ReflectionUtils.invokeMethod.
     * This does not work at the moment due to an issue with exception handling in that method
     * which needs further analysis. 
     */
    private static Object uncheckedInvoke(Method method, Object target, Object... args)
    {
        try
        {
            return method.invoke(target, args);
        }
        catch (IllegalAccessException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e.getCause());
        }
    }

    public static Object getEntityGraph(EntityManager em, Class<?> entityClass, EntityGraph entityGraphAnn)
    {
        ensureAvailable();
        String graphName = entityGraphAnn.value();
        if (graphName.isEmpty())
        {
            return buildEntityGraph(em, entityClass, entityGraphAnn.paths());
        }
        else
        {
            return uncheckedInvoke(EM_GET_ENTITY_GRAPH, em, graphName);
        }
    }

    private static Object createEntityGraph(EntityManager em, Class<?> entityClass)
    {
        return uncheckedInvoke(EM_CREATE_ENTITY_GRAPH, em, entityClass);
    }

    private static void ensureAvailable()
    {
        if (!isAvailable())
        {
            throw new EntityGraphException("Class java.persistence.EntityGraph is not available. "
                + "Does your PersistenceProvider support JPA 2.1?");
        }
    }

    private static Object addSubgraph(Object graph, String attributeName)
    {
        if (ENTITY_GRAPH_CLASS.isInstance(graph))
        {
            return uncheckedInvoke(EG_ADD_SUBGRAPH, graph, attributeName);
        }
        else if (SUBGRAPH_CLASS.isInstance(graph))
        {
            return uncheckedInvoke(SUBGRAPH_ADD_SUBGRAPH, graph, attributeName);
        }
        return null;
    }

    private static void addAttributeNodes(Object graph, String attributeName)
    {
        if (ENTITY_GRAPH_CLASS.isInstance(graph))
        {
            uncheckedInvoke(EG_ADD_ATTRIBUTE_NODES, graph,
                new Object[] { new String[] { attributeName } });
        }
        else if (SUBGRAPH_CLASS.isInstance(graph))
        {
            uncheckedInvoke(SUBGRAPH_ADD_ATTRIBUTE_NODES, graph,
                new Object[] { new String[] { attributeName } });
        }
    }

    private static Object buildEntityGraph(EntityManager em, Class<?> entityClass,
        String[] attributePaths)
    {
        Object graph = createEntityGraph(em, entityClass);
        List<String> paths = new ArrayList<String>(Arrays.asList(attributePaths));

        // handle longest paths first
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
