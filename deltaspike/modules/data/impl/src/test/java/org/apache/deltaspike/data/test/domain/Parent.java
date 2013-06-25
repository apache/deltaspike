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
package org.apache.deltaspike.data.test.domain;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;

@Entity
public class Parent extends NamedEntity
{

    @javax.persistence.OneToOne(cascade = CascadeType.ALL)
    private OneToOne one;

    @javax.persistence.OneToMany(cascade = CascadeType.ALL, targetEntity = OneToMany.class)
    private List<OneToMany> many = new LinkedList<OneToMany>();

    private Long value = Long.valueOf(0);

    public Parent()
    {
        super();
    }

    public Parent(String name)
    {
        super(name);
    }

    @Override
    public String toString()
    {
        return "Parent [value=" + value + ", getName()=" + getName() + ", getId()=" + getId() + "]";
    }

    public void add(OneToMany otm)
    {
        many.add(otm);
    }

    public OneToOne getOne()
    {
        return one;
    }

    public void setOne(OneToOne one)
    {
        this.one = one;
    }

    public List<OneToMany> getMany()
    {
        return many;
    }

    public void setMany(List<OneToMany> many)
    {
        this.many = many;
    }

    public Long getValue()
    {
        return value;
    }

    public void setValue(Long value)
    {
        this.value = value;
    }

}
