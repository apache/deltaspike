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
package org.apache.deltaspike.core.impl.scope.conversation;

import org.apache.deltaspike.core.api.scope.ConversationGroup;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.inject.Named;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ConversationKey implements Serializable
{
    private static final long serialVersionUID = 6565204223928766263L;

    private final Class<?> groupKey;

    //HashSet due to Serializable warning in checkstyle rules
    private HashSet<Annotation> qualifiers;

    public ConversationKey(Class<?> groupKey, Annotation... qualifiers)
    {
        this.groupKey = groupKey;

        //TODO maybe we have to add a real qualifier instead
        Class<? extends Annotation> annotationType;
        for (Annotation qualifier : qualifiers)
        {
            annotationType = qualifier.annotationType();

            if (Any.class.isAssignableFrom(annotationType) ||
                    Default.class.isAssignableFrom(annotationType) ||
                    Named.class.isAssignableFrom(annotationType) ||
                    ConversationGroup.class.isAssignableFrom(annotationType))
            {
                //won't be used for this key!
                continue;
            }

            if (this.qualifiers == null)
            {
                this.qualifiers = new HashSet<Annotation>();
            }
            this.qualifiers.add(qualifier);
        }
    }

    public Class<?> getConversationGroup()
    {
        return groupKey;
    }

    public Set<Annotation> getQualifiers()
    {
        if (qualifiers == null)
        {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(this.qualifiers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ConversationKey))
        {
            return false;
        }

        ConversationKey that = (ConversationKey) o;

        if (!groupKey.equals(that.groupKey))
        {
            return false;
        }
        if (qualifiers == null && that.qualifiers == null)
        {
            return true;
        }
        if (qualifiers != null && that.qualifiers == null)
        {
            return false;
        }

        if (!that.qualifiers.equals(qualifiers))
        {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int result = groupKey.hashCode();
        result = 31 * result + (qualifiers != null ? qualifiers.hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("conversation-key\n");

        result.append("\n");
        result.append("\tgroup:\t\t");
        result.append(this.groupKey.getName());

        result.append("\n");
        result.append("\tqualifiers:\t");

        if (qualifiers != null)
        {
            for (Annotation qualifier : this.qualifiers)
            {
                result.append(qualifier.annotationType().getName());
                result.append(" ");
            }
        }
        else
        {
            result.append("---");
        }

        return result.toString();
    }
}
