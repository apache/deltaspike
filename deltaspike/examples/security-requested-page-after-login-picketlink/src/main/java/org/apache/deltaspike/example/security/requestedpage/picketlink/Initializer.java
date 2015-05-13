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
package org.apache.deltaspike.example.security.requestedpage.picketlink;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.User;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * This startup bean creates the default users, groups and roles when the application is started.
 */
@Singleton
@Startup
public class Initializer
{

    @Inject
    private PartitionManager partitionManager;

    @PostConstruct
    public void create() 
    {

        // Create user john
        User john = new User("john");
        john.setEmail("john@acme.com");
        john.setFirstName("John");
        john.setLastName("User");

        IdentityManager identityManager = this.partitionManager.createIdentityManager();

        identityManager.add(john);
        identityManager.updateCredential(john, new Password("123456"));

    }
}
