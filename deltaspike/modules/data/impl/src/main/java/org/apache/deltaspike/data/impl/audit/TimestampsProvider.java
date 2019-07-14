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

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import org.apache.deltaspike.data.api.audit.CreatedOn;
import org.apache.deltaspike.data.api.audit.ModifiedOn;
import org.apache.deltaspike.data.impl.property.Property;

/**
 * Set timestamps on marked properties.
 */
class TimestampsProvider extends AuditProvider
{

    @Override
    public void prePersist(Object entity)
    {
        updateTimestamps(entity, true);
    }

    @Override
    public void preUpdate(Object entity)
    {
        updateTimestamps(entity, false);
    }

    private void updateTimestamps(Object entity, boolean create)
    {
        long systime = System.currentTimeMillis();
        for (Property<Object> property : getProperties(entity, CreatedOn.class, ModifiedOn.class, create))
        {
            setProperty(entity, property, systime, create);
        }
    }

    private void setProperty(Object entity, Property<Object> property, long systime, boolean create)
    {
        try
        {
            if (!isCorrectContext(property, create))
            {
                return;
            }
            Object now = now(property.getJavaClass(), systime);
            property.setValue(entity, now);
            log.log(Level.FINER, "Updated property {0} with {1}", new Object[] { propertyName(entity, property), now });
        }
        catch (Exception e)
        {
            String message = "Failed to set property " + propertyName(entity, property) + ", is this a temporal type?";
            throw new AuditPropertyException(message, e);
        }
    }

    private boolean isCorrectContext(Property<Object> property, boolean create)
    {
        if (create && property.getAnnotatedElement().isAnnotationPresent(ModifiedOn.class))
        {
            ModifiedOn annotation = property.getAnnotatedElement().getAnnotation(ModifiedOn.class);
            if (!annotation.onCreate())
            {
                return false;
            }
        }
        return true;
    }

    private Object now(Class<?> field, long systime) throws Exception
    {
        if (isCalendarClass(field))
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(systime);
            return cal;
        }
        else if (isDateClass(field))
        {
            return field.getConstructor(Long.TYPE).newInstance(systime);
        }
        throw new IllegalArgumentException("Annotated field is not a date class: " + field);
    }

    private boolean isCalendarClass(Class<?> field)
    {
        return Calendar.class.isAssignableFrom(field);
    }

    private boolean isDateClass(Class<?> field)
    {
        return Date.class.isAssignableFrom(field);
    }

}
