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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.deltaspike.data.api.audit.CreatedBy;
import org.apache.deltaspike.data.api.audit.CreatedOn;
import org.apache.deltaspike.data.api.audit.ModifiedBy;
import org.apache.deltaspike.data.api.audit.ModifiedOn;

@Entity
@SuppressWarnings("serial")
public class AuditedEntity implements Serializable
{

    @Id
    @GeneratedValue
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @CreatedOn
    private Calendar created;

    private String name;

    @CreatedBy
    private String creator;

    @CreatedBy
    @ManyToOne(targetEntity = Principal.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Principal creatorPrincipal;

    @ModifiedBy
    private String changer;

    @ModifiedBy
    @ManyToOne(targetEntity = Principal.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Principal principal;

    @ModifiedBy(onCreate = false)
    private String changerOnly;

    @ModifiedBy(onCreate = false)
    @ManyToOne(targetEntity = Principal.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Principal changerOnlyPrincipal;

    @Temporal(TemporalType.TIME)
    @ModifiedOn(onCreate = true)
    private java.util.Date modified;

    @Temporal(TemporalType.DATE)
    @ModifiedOn
    private Calendar gregorianModified;

    @ModifiedOn
    private Timestamp timestamp;

    public AuditedEntity()
    {
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Calendar getCreated()
    {
        return created;
    }

    public java.util.Date getModified()
    {
        return modified;
    }

    public Calendar getGregorianModified()
    {
        return gregorianModified;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Timestamp getTimestamp()
    {
        return timestamp;
    }

    public String getChanger()
    {
        return changer;
    }

    public void setChanger(String changer)
    {
        this.changer = changer;
    }

    public Principal getPrincipal()
    {
        return principal;
    }

    public String getCreator() {
        return creator;
    }

    public Principal getCreatorPrincipal() {
        return creatorPrincipal;
    }

    public String getChangerOnly() {
        return changerOnly;
    }

    public Principal getChangerOnlyPrincipal() {
        return changerOnlyPrincipal;
    }
}
