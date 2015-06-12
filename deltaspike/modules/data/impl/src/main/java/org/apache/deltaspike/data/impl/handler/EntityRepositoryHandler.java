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
package org.apache.deltaspike.data.impl.handler;

import org.apache.deltaspike.core.util.StringUtils;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.meta.RequiresTransaction;
import org.apache.deltaspike.data.impl.meta.unit.PersistenceUnits;
import org.apache.deltaspike.data.impl.property.Property;
import org.apache.deltaspike.data.impl.property.query.NamedPropertyCriteria;
import org.apache.deltaspike.data.impl.property.query.PropertyQueries;
import org.apache.deltaspike.data.impl.util.EntityUtils;
import org.apache.deltaspike.data.spi.DelegateQueryHandler;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.deltaspike.data.impl.util.QueryUtils.isEmpty;
import static org.apache.deltaspike.data.impl.util.QueryUtils.isString;

/**
 * Implement basic functionality from the {@link EntityRepository}.
 *
 * @param <E>  Entity type.
 * @param <PK> Primary key type, must be a serializable.
 */
public class EntityRepositoryHandler<E, PK extends Serializable>
        implements EntityRepository<E, PK>, DelegateQueryHandler
{

    private static final Logger log = Logger.getLogger(EntityRepositoryHandler.class.getName());

    @Inject
    private CdiQueryInvocationContext context;

    @Override
    @RequiresTransaction
    public E save(E entity)
    {
        if (context.isNew(entity))
        {
            entityManager().persist(entity);
            return entity;
        }
        return entityManager().merge(entity);
    }

    @Override
    @RequiresTransaction
    public E saveAndFlush(E entity)
    {
        E result = save(entity);
        flush();
        return result;
    }

    @Override
    @RequiresTransaction
    public E saveAndFlushAndRefresh(E entity)
    {
        E result = saveAndFlush(entity);
        entityManager().refresh(result);
        return result;
    }

    @Override
    @RequiresTransaction
    public void refresh(E entity)
    {
        entityManager().refresh(entity);
    }

    @Override
    public E findBy(PK primaryKey)
    {
        return entityManager().find(entityClass(), primaryKey);
    }

    @Override
    public List<E> findBy(E example, SingularAttribute<E, ?>... attributes)
    {
        return findBy(example, -1, -1, attributes);
    }

    @Override
    public List<E> findBy(E example, int start, int max, SingularAttribute<E, ?>... attributes)
    {
        return executeExampleQuery(example, start, max, false, attributes);
    }

    @Override
    public List<E> findByLike(E example, SingularAttribute<E, ?>... attributes)
    {
        return findByLike(example, -1, -1, attributes);
    }

    @Override
    public List<E> findByLike(E example, int start, int max, SingularAttribute<E, ?>... attributes)
    {
        return executeExampleQuery(example, start, max, true, attributes);
    }

    @Override
    public List<E> findAll()
    {
        return context.applyRestrictions(entityManager().createQuery(allQuery(), entityClass())).getResultList();
    }

    @Override
    public List<E> findAll(int start, int max)
    {
        TypedQuery<E> query = entityManager().createQuery(allQuery(), entityClass());
        if (start > 0)
        {
            query.setFirstResult(start);
        }
        if (max > 0)
        {
            query.setMaxResults(max);
        }
        return context.applyRestrictions(query).getResultList();
    }

    @Override
    public Long count()
    {
        return (Long) context.applyRestrictions(entityManager().createQuery(countQuery(), Long.class))
                .getSingleResult();
    }

    @Override
    public Long count(E example, SingularAttribute<E, ?>... attributes)
    {
        return executeCountQuery(example, false, attributes);
    }

    @Override
    public Long countLike(E example, SingularAttribute<E, ?>... attributes)
    {
        return executeCountQuery(example, true, attributes);
    }

    @Override
    @RequiresTransaction
    public void remove(E entity)
    {
        entityManager().remove(entity);
    }

    @Override
    @RequiresTransaction
    public void removeAndFlush(E entity)
    {
        entityManager().remove(entity);
        flush();
    }

    @Override
    @RequiresTransaction
    public void attachAndRemove(E entity)
    {
        if (!entityManager().contains(entity))
        {
            entity = entityManager().merge(entity);
        }
        remove(entity);
    }

    @Override
    @RequiresTransaction
    public void flush()
    {
        entityManager().flush();
    }

    public EntityManager entityManager()
    {
        return context.getEntityManager();
    }

    public CriteriaQuery<E> criteriaQuery()
    {
        return entityManager().getCriteriaBuilder().createQuery(entityClass());
    }

    public TypedQuery<E> typedQuery(String qlString)
    {
        return entityManager().createQuery(qlString, entityClass());
    }

    @SuppressWarnings("unchecked")
    public Class<E> entityClass()
    {
        return (Class<E>) context.getEntityClass();
    }

    public String tableName()
    {
        final Class<?> entityClass = context.getEntityClass();
        final String tableName = PersistenceUnits.instance().entityTableName(entityClass);
        if (StringUtils.isEmpty(tableName))
        {
            final EntityType<?> entityType = entityManager().getMetamodel().entity(entityClass);
            Table tableAnnotation = entityClass.getAnnotation(Table.class);
            return (tableAnnotation == null)
                    ? entityType.getName()
                    : tableAnnotation.name();
        }
        return tableName;
    }

    public String entityName()
    {
        return EntityUtils.entityName(entityClass());
    }

    // ----------------------------------------------------------------------------
    // PRIVATE
    // ----------------------------------------------------------------------------

    private String allQuery()
    {
        return QueryBuilder.selectQuery(entityName());
    }

    private String countQuery()
    {
        return QueryBuilder.countQuery(entityName());
    }

    private String exampleQuery(String queryBase, List<Property<Object>> properties, boolean useLikeOperator)
    {
        StringBuilder jpqlQuery = new StringBuilder(queryBase).append(" where ");
        jpqlQuery.append(prepareWhere(properties, useLikeOperator));
        return jpqlQuery.toString();
    }

    private void addParameters(TypedQuery<?> query, E example, List<Property<Object>> properties,
                               boolean useLikeOperator)
    {
        for (Property<Object> property : properties)
        {
            property.setAccessible();
            query.setParameter(property.getName(), transform(property.getValue(example), useLikeOperator));
        }
    }

    private Object transform(Object value, final boolean useLikeOperator)
    {
        if (value != null && useLikeOperator && isString(value))
        {
            // seems to be an OpenJPA bug:
            // parameters in querys fail validation, e.g. UPPER(e.name) like UPPER(:name)
            String result = ((String) value).toUpperCase();
            return "%" + result + "%";
        }
        return value;
    }

    private String prepareWhere(List<Property<Object>> properties, boolean useLikeOperator)
    {
        Iterator<Property<Object>> iterator = properties.iterator();
        StringBuilder result = new StringBuilder();
        while (iterator.hasNext())
        {
            Property<Object> property = iterator.next();
            String name = property.getName();
            if (useLikeOperator && property.getJavaClass().getName().equals(String.class.getName()))
            {
                result.append("UPPER(e.").append(name).append(") like :").append(name)
                        .append(iterator.hasNext() ? " and " : "");
            }
            else
            {
                result.append("e.").append(name).append(" = :").append(name).append(iterator.hasNext() ? " and " : "");
            }
        }
        return result.toString();
    }

    private List<String> extractPropertyNames(SingularAttribute<E, ?>... attributes)
    {
        List<String> result = new ArrayList<String>(attributes.length);
        for (SingularAttribute<E, ?> attribute : attributes)
        {
            result.add(attribute.getName());
        }
        return result;
    }

    private List<Property<Object>> extractProperties(SingularAttribute<E, ?>... attributes)
    {
        List<String> names = extractPropertyNames(attributes);
        List<Property<Object>> properties = PropertyQueries.createQuery(entityClass())
                .addCriteria(new NamedPropertyCriteria(names.toArray(new String[]{}))).getResultList();
        return properties;
    }

    private List<E> executeExampleQuery(E example, int start, int max, boolean useLikeOperator,
                                        SingularAttribute<E, ?>... attributes)
    {
        // Not sure if this should be the intended behaviour
        // when we don't get any attributes maybe we should
        // return a empty list instead of all results
        if (isEmpty(attributes))
        {
            return findAll(start, max);
        }

        List<Property<Object>> properties = extractProperties(attributes);
        String jpqlQuery = exampleQuery(allQuery(), properties, useLikeOperator);
        log.log(Level.FINER, "findBy|findByLike: Created query {0}", jpqlQuery);
        TypedQuery<E> query = entityManager().createQuery(jpqlQuery, entityClass());

        // set starting position
        if (start > 0)
        {
            query.setFirstResult(start);
        }

        // set maximum results
        if (max > 0)
        {
            query.setMaxResults(max);
        }

        context.applyRestrictions(query);
        addParameters(query, example, properties, useLikeOperator);
        return query.getResultList();
    }

    private Long executeCountQuery(E example, boolean useLikeOperator, SingularAttribute<E, ?>... attributes)
    {
        if (isEmpty(attributes))
        {
            return count();
        }
        List<Property<Object>> properties = extractProperties(attributes);
        String jpqlQuery = exampleQuery(countQuery(), properties, useLikeOperator);
        log.log(Level.FINER, "count: Created query {0}", jpqlQuery);
        TypedQuery<Long> query = entityManager().createQuery(jpqlQuery, Long.class);
        addParameters(query, example, properties, useLikeOperator);
        context.applyRestrictions(query);
        return query.getSingleResult();
    }
}
