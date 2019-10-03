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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.util.EntityUtils;

@ApplicationScoped
public class EntityMetadataInitializer
{
    private static final Logger LOG = Logger.getLogger(EntityMetadataInitializer.class.getName());
    
    public EntityMetadata init(RepositoryMetadata metadata)
    {
        EntityMetadata entityMetadata = extract(metadata.getRepositoryClass());
        
        entityMetadata.setPrimaryKeyProperty(EntityUtils.primaryKeyProperty(entityMetadata.getEntityClass()));
        entityMetadata.setVersionProperty(EntityUtils.getVersionProperty(entityMetadata.getEntityClass()));
        entityMetadata.setEntityName(EntityUtils.entityName(entityMetadata.getEntityClass()));

        return entityMetadata;
    }
    
    private EntityMetadata extract(Class<?> repositoryClass)
    {
        // get from annotation
        Repository repository = repositoryClass.getAnnotation(Repository.class);
        Class<?> entityClass = repository.forEntity();
        boolean isEntityClass = !Object.class.equals(entityClass) && EntityUtils.isEntityClass(entityClass);
        if (isEntityClass)
        {
            return new EntityMetadata(entityClass, EntityUtils.primaryKeyClass(entityClass));
        }
        
        // get from type
        for (Type inf : repositoryClass.getGenericInterfaces())
        {
            EntityMetadata result = extractFromType(inf);
            if (result != null)
            {
                return result;
            }
        }

        EntityMetadata entityMetadata = extractFromType(repositoryClass.getGenericSuperclass());
        if (entityMetadata != null)
        {
            return entityMetadata;
        }
        for (Type intf : repositoryClass.getGenericInterfaces())
        {
            entityMetadata = extract( (Class< ? >)intf );
            if (entityMetadata != null)
            {
                return entityMetadata;
            }
        }
        if (repositoryClass.getSuperclass() != null)
        {
            return extract(repositoryClass.getSuperclass());
        }
        return null;
    }

    private EntityMetadata extractFromType(Type type)
    {
        LOG.log(Level.FINER, "extractFrom: type = {0}", type);

        if (!(type instanceof ParameterizedType))
        {
            return null;
        }
        
        ParameterizedType parametrizedType = (ParameterizedType) type;
        Type[] genericTypes = parametrizedType.getActualTypeArguments();
        
        EntityMetadata result = null;
        
        // don't use a foreach here, we must be sure that the we first get the entity type
        for (Type genericType : genericTypes)
        {
            if (genericType instanceof Class && EntityUtils.isEntityClass((Class<?>) genericType))
            {
                result = new EntityMetadata((Class<?>) genericType);
                continue;
            }

            if (result != null && genericType instanceof Class)
            {
                result.setPrimaryKeyClass((Class<? extends Serializable>) genericType);
                return result;
            }
        }
        return result;
    }
}
