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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.apache.deltaspike.data.api.EntityGraph;

/**
 * Helper for entity graphs.
 */
public final class EntityGraphHelper
{
    private EntityGraphHelper()
    {

    }
    
    public static Object getEntityGraph(EntityManager em, Class<?> entityClass, EntityGraph entityGraphAnn)
    {
        String graphName = entityGraphAnn.value();
        if (graphName.isEmpty())
        {
            return buildEntityGraph(em, entityClass, entityGraphAnn.paths());
        }
        else
        {
            return em.getEntityGraph(graphName);
        }
    }

    private static Object createEntityGraph(EntityManager em, Class<?> entityClass)
    {
        return em.createEntityGraph(entityClass);
    }

    private static Object addSubgraph(Object graph, String attributeName)
    {
        if (graph instanceof jakarta.persistence.EntityGraph)
        {
            return ((jakarta.persistence.EntityGraph) graph).addSubgraph(attributeName);
        }
        else if (graph instanceof jakarta.persistence.Subgraph)
        {
            return ((jakarta.persistence.Subgraph) graph).addSubgraph(attributeName);
        }

        return null;
    }

    private static void addAttributeNodes(Object graph, String attributeName)
    {
        if (graph instanceof jakarta.persistence.EntityGraph)
        {
            ((jakarta.persistence.EntityGraph) graph).addAttributeNodes(
                    new String[] { attributeName });
        }
        else if (graph instanceof jakarta.persistence.Subgraph)
        {
            ((jakarta.persistence.Subgraph) graph).addAttributeNodes(
                    new String[] { attributeName });
        }
    }

    private static Object buildEntityGraph(EntityManager em, Class<?> entityClass,
        String[] attributePaths)
    {
        Object graph = createEntityGraph(em, entityClass);
        List<String> paths = new ArrayList<>(Arrays.asList(attributePaths));

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
