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
package org.apache.deltaspike.data.test.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;
import org.apache.deltaspike.data.test.domain.OneToMany;
import org.apache.deltaspike.data.test.domain.OneToMany_;
import org.apache.deltaspike.data.test.domain.OneToOne;
import org.apache.deltaspike.data.test.domain.OneToOne_;
import org.apache.deltaspike.data.test.domain.Parent;
import org.apache.deltaspike.data.test.domain.Parent_;

@Repository
public abstract class ParentRepository extends AbstractEntityRepository<Parent, Long>
        implements CriteriaSupport<Parent>
{

    public List<Parent> joinQuery(String name, String oneName, String manyName)
    {
        return criteria()
                .eq(Parent_.name, name)
                .join(Parent_.one,
                        where(OneToOne.class, JoinType.LEFT)
                                .eq(OneToOne_.name, oneName)
                )
                .join(Parent_.many,
                        where(OneToMany.class)
                                .eq(OneToMany_.name, manyName)
                )
                .createQuery()
                .getResultList();
    }

    public List<Parent> nullAwareQuery(String name1, String name2, Long counter)
    {
        return criteria()
                .eq(Parent_.name, name1)
                .eq(Parent_.name, name2)
                .eq(Parent_.value, counter)
                .createQuery()
                .getResultList();
    }

    public Parent fetchQuery(String name)
    {
        return criteria()
                .eq(Parent_.name, name)
                .fetch(Parent_.many)
                .distinct()
                .createQuery()
                .getSingleResult();
    }

    public List<Parent> fetchByName(String name1, String name2, String name3)
    {
        return criteria()
                .in(Parent_.name, name1, name2, name3)
                .createQuery()
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Parent> orQuery(String name1, String name2)
    {
        return criteria()
                .or(
                        criteria()
                                .eq(Parent_.name, name2)
                                .between(Parent_.value, 50L, 100L),
                        criteria()
                                .eq(Parent_.name, name1)
                                .between(Parent_.value, 0L, 50L),
                        criteria()
                                .eq(Parent_.name, "does not exist!")
                )
                .createQuery()
                .getResultList();
    }

    public List<Parent> orderedQuery()
    {
        return criteria()
                .orderAsc(Parent_.name)
                .createQuery()
                .getResultList();
    }

}
