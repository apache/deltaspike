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
package org.apache.deltaspike.security.impl.idm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.deltaspike.security.api.idm.IdentityType;

/**
 * Abstract base class for IdentityType implementations 
 */
public abstract class AbstractIdentityType implements IdentityType
{
    private String key;
    private boolean enabled = true;
    private Date creationDate = null;
    private Date expirationDate = null;
    private Map<String,String[]> attributes = new HashMap<String,String[]>();
    
    @Override
    public String getKey()
    {
        return this.key;
    }

    @Override
    public boolean isEnabled()
    {
        return this.enabled;
    }

    @Override
    public Date getExpirationDate()
    {
        return this.expirationDate;
    }

    @Override
    public Date getCreationDate()
    {
        return this.creationDate;
    }

    @Override
    public void setAttribute(String name, String value)
    {
        attributes.put(name, new String[]{value});        
    }

    @Override
    public void setAttribute(String name, String[] values)
    {
        attributes.put(name,  values);
    }

    @Override
    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    @Override
    public String getAttribute(String name)
    {
        String[] vals = attributes.get(name);
        return null == vals ? null : ((vals.length != 0) ? vals[0] : null);
    }

    @Override
    public String[] getAttributeValues(String name)
    {
        return attributes.get(name);
    }

    @Override
    public Map<String, String[]> getAttributes()
    {
        return java.util.Collections.unmodifiableMap(attributes);
    }

}
