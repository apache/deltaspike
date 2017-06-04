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
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
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
     * Repository access - lookup the Repository component meta data from a list of candidate classes.
     * Depending on the implementation, proxy objects might have been modified so the actual class
     * does not match the original Repository class.
     *
     * @param candidateClasses  List of candidates to check.
     * @return A {@link RepositoryMetadataInitializer} corresponding to the repoClass parameter.
     */
    public RepositoryMetadata lookupComponent(List<Class<?>> candidateClasses)
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
     * Repository access - lookup the Repository component meta data for a specific Repository class.
     *
     * @param repoClass  The Repository class to lookup the method for
     * @return A {@link RepositoryMetadataInitializer} corresponding to the repoClass parameter.
     */
    public RepositoryMetadata lookupComponent(Class<?> repoClass)
    {
        if (repositoriesMetadata.containsKey(repoClass))
        {
            return repositoriesMetadata.get(repoClass);
        }
        throw new RuntimeException("Unknown Repository class " + repoClass.getName());
    }

    /**
     * Repository access - lookup method information for a specific Repository class.
     *
     * @param repoClass The Repository class to lookup the method for
     * @param method    The Method object to get Repository meta data for.
     * @return A {@link RepositoryMethodMetadataInitializer} corresponding to the method parameter.
     */
    public RepositoryMethodMetadata lookupMethod(Class<?> repoClass, Method method)
    {
        return lookupComponent(repoClass).getMethodsMetadata().get(method);
    }
    
    public RepositoryMethodMetadata lookupMethod(RepositoryMetadata metadata, Method method)
    {
        return metadata.getMethodsMetadata().get(method);
    }

    public Map<Class<?>, RepositoryMetadata> getRepositories()
    {
        return repositoriesMetadata;
    }
}
