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
package org.apache.deltaspike.data.impl.audit;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.deltaspike.data.impl.property.Property;
import org.apache.deltaspike.data.impl.property.query.AnnotatedPropertyCriteria;
import org.apache.deltaspike.data.impl.property.query.PropertyQueries;
import org.apache.deltaspike.data.impl.property.query.PropertyQuery;

abstract class AuditProvider implements PrePersistAuditListener, PreUpdateAuditListener
{

    protected static final Logger log = Logger.getLogger(AuditProvider.class.getName());

    String propertyName(Object entity, Property<Object> property)
    {
        return entity.getClass().getSimpleName() + "." + property.getName();
    }

    List<Property<Object>> getProperties(
            Object entity,
            Class<? extends Annotation> createdAnnotation,
            Class<? extends Annotation> modifiedAnnotation,
            boolean create)
    {
        List<Property<Object>> properties = new LinkedList<>();
        PropertyQuery<Object> query = PropertyQueries.createQuery(entity.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(modifiedAnnotation));
        properties.addAll(query.getWritableResultList());
        if (create)
        {
            query = PropertyQueries.<Object> createQuery(entity.getClass())
                    .addCriteria(new AnnotatedPropertyCriteria(createdAnnotation));
            properties.addAll(query.getWritableResultList());
        }
        return properties;
    }
}
