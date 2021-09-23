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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import org.apache.deltaspike.data.impl.RepositoryExtension;

@ApplicationScoped
public class RepositoryMetadataHandler
{
    private final Map<Class<?>, RepositoryMetadata> repositoriesMetadata =
            new ConcurrentHashMap<Class<?>, RepositoryMetadata>();

    @Inject
    private BeanManager beanManager;
    @Inject
    private RepositoryExtension extension;
    
    @Inject
    private RepositoryMetadataInitializer metadataInitializer;

    @PostConstruct
    public void init()
    {
        for (Class<?> repositoryClass : extension.getRepositoryClasses())
        {
            RepositoryMetadata metadata = metadataInitializer.init(repositoryClass, beanManager);
            repositoriesMetadata.put(repositoryClass, metadata);
        }
    }

    /**
     * Lookup the Repository component meta data from a list of candidate classes.
     * Depending on the implementation, proxy objects might have been modified so the actual class
     * does not match the original Repository class.
     *
     * @param candidateClasses List of candidates to check.
     * @return A {@link RepositoryMetadataInitializer}.
     */
    public RepositoryMetadata lookupMetadata(List<Class<?>> candidateClasses)
    {
        for (Class<?> repoClass : candidateClasses)
        {
            if (repositoriesMetadata.containsKey(repoClass))
            {
                return repositoriesMetadata.get(repoClass);
            }
        }
        throw new RuntimeException("Unknown Repository classes " + candidateClasses);
    }
    
    /**
     * lookup the {@link RepositoryMethodMetadata} for a specific repository and method.
     *
     * @param repositoryMetadata The Repository metadata to lookup the method for.
     * @param method The method object to get Repository meta data for.
     * @return A {@link RepositoryMethodMetadata}.
     */
    public RepositoryMethodMetadata lookupMethodMetadata(RepositoryMetadata repositoryMetadata, Method method)
    {
        return repositoryMetadata.getMethodsMetadata().get(method);
    }

}
