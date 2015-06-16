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

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = SimpleStringId.FIND_ALL_ORDER_BY_ID, query = "SELECT e FROM SimpleStringId e ORDER BY e.id")
})
@Table(name = "SIMPLE_TABLE_STRING")
public class SimpleStringId extends SuperSimple
{

    public static final String FIND_ALL_ORDER_BY_ID = "SimpleStringId.findAllOrderById";

    @Id
    private String id;
    private String name;

    public SimpleStringId()
    {

    }

    public SimpleStringId(String id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
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

    @Override
    public String toString()
    {
        return "Simple [id=" + id + ", name=" + name + "]";
    }
}
