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
package org.apache.deltaspike.security.api.permission;

import org.apache.deltaspike.security.api.idm.IdentityType;

/**
 * Represents a specific permission grant for a domain object 
 *
 */
public class Permission
{
    private Object resource;
    private IdentityType recipient;
    private String permission;
    
    public Permission(Object resource, IdentityType recipient, String permission)
    {
        this.resource = resource;
        this.recipient = recipient;
        this.permission = permission;
    }
    
    public Object getResource()
    {
        return resource;
    }
    
    public IdentityType getRecipient()
    {
        return recipient;        
    }
    
    public String getPermission()
    {
        return permission;
    }
}
