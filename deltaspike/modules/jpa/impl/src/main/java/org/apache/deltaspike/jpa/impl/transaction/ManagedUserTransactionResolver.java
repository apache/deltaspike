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
package org.apache.deltaspike.jpa.impl.transaction;

import javax.annotation.Resource;
import javax.enterprise.context.Dependent;
import javax.transaction.UserTransaction;

//we need to keep it separated,
//because the injection of UserTransaction might fail with an exception (see DELTASPIKE-986)
@Dependent
public class ManagedUserTransactionResolver
{
    @Resource
    private UserTransaction userTransaction;

    public UserTransaction resolveUserTransaction()
    {
        return userTransaction;
    }
}
