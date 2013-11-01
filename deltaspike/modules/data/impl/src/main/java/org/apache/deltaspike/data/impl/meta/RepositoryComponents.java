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
package org.apache.deltaspike.data.impl.meta;

import org.apache.deltaspike.data.impl.RepositoryDefinitionException;
import org.apache.deltaspike.data.impl.meta.extractor.AnnotationMetadataExtractor;
import org.apache.deltaspike.data.impl.meta.extractor.MetadataExtractor;
import org.apache.deltaspike.data.impl.meta.extractor.TypeMetadataExtractor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class to access Repository and Repository method meta data.
 * Acts as repository for Repository related meta data.
 */
public class RepositoryComponents implements Serializable
{

    private static final long serialVersionUID = 1L;

    private final Map<Class<?>, RepositoryComponent> repos = new HashMap<Class<?>, RepositoryComponent>();

    private final List<MetadataExtractor> extractors = Arrays.asList(new AnnotationMetadataExtractor(),
            new TypeMetadataExtractor());

    /**
     * Add a Repository class to the meta data repository.
     *
     *
     * @param repoClass  The repo class.
     * @return {@code true} if Repository class has been added, {@code false} otherwise.
     */
    public void add(Class<?> repoClass)
    {
        RepositoryEntity entityClass = extractEntityMetaData(repoClass);
        RepositoryComponent repo = new RepositoryComponent(repoClass, entityClass);
        repos.put(repoClass, repo);
    }

    /**
     * Repository access - lookup the Repository component meta data from a list of candidate classes.
     * Depending on the implementation, proxy objects might have been modified so the actual class
     * does not match the original Repository class.
     *
     * @param candidateClasses  List of candidates to check.
     * @return A {@link RepositoryComponent} corresponding to the repoClass parameter.
     */
    public RepositoryComponent lookupComponent(List<Class<?>> candidateClasses)
    {
        for (Class<?> repoClass : candidateClasses)
        {
            if (repos.containsKey(repoClass))
            {
                return repos.get(repoClass);
            }
        }
        throw new RuntimeException("Unknown Repository classes " + candidateClasses);
    }

    /**
     * Repository access - lookup the Repository component meta data for a specific Repository class.
     *
     * @param repoClass  The Repository class to lookup the method for
     * @return A {@link RepositoryComponent} corresponding to the repoClass parameter.
     */
    public RepositoryComponent lookupComponent(Class<?> repoClass)
    {
        if (repos.containsKey(repoClass))
        {
            return repos.get(repoClass);
        }
        throw new RuntimeException("Unknown Repository class " + repoClass.getName());
    }

    /**
     * Repository access - lookup method information for a specific Repository class.
     *
     * @param repoClass The Repository class to lookup the method for
     * @param method    The Method object to get Repository meta data for.
     * @return A {@link RepositoryMethod} corresponding to the method parameter.
     */
    public RepositoryMethod lookupMethod(Class<?> repoClass, Method method)
    {
        return lookupComponent(repoClass).lookupMethod(method);
    }

    private RepositoryEntity extractEntityMetaData(Class<?> repoClass)
    {
        for (MetadataExtractor extractor : extractors)
        {
            RepositoryEntity entity = extractor.extract(repoClass);
            if (entity != null)
            {
                return entity;
            }
        }
        throw new RepositoryDefinitionException(repoClass);
    }

}
