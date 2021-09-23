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
package org.apache.deltaspike.data.test.ee7.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@NamedEntityGraphs({
    @NamedEntityGraph(name = "withFlats", attributeNodes = @NamedAttributeNode("flats")),
    @NamedEntityGraph(name = "withGarages", attributeNodes = @NamedAttributeNode("garages"))
})
@Entity
@Table
public class House implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL)
    @OrderColumn
    private List<Flat> flats = new ArrayList<Flat>();

    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL)
    @OrderColumn
    private List<Garage> garages = new ArrayList<Garage>();

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Flat> getFlats()
    {
        return flats;
    }

    public void setFlats(List<Flat> flats)
    {
        this.flats = flats;
    }

    public List<Garage> getGarages()
    {
        return garages;
    }

    public void setGarages(List<Garage> garages)
    {
        this.garages = garages;
    }
}
