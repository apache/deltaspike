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
package org.apache.deltaspike.data.impl.criteria.predicate;

import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;

public class In<P, V> implements PredicateBuilder<P>
{

    private final SingularAttribute<? super P, V> singular;
    private final V[] values;

    public In(SingularAttribute<? super P, V> singular, V[] values)
    {
        this.singular = singular;
        this.values = Arrays.copyOf(values, values.length);
    }

    @Override
    public List<Predicate> build(CriteriaBuilder builder, Path<P> path)
    {
        Path<V> p = path.get(singular);
        CriteriaBuilder.In<V> in = builder.in(p);
        for (V value : values)
        {
            if (value != null)
            {
                in.value(value);
            }
        }
        return Arrays.asList((Predicate) in);
    }

    SingularAttribute<? super P, V> getSingular()
    {
        return singular;
    }

    V[] getValues()
    {
        return values;
    }

}
