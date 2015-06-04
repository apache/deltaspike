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

import org.apache.deltaspike.core.impl.util.JndiUtils;

import javax.annotation.Resource;
import javax.enterprise.context.Dependent;
import javax.transaction.UserTransaction;
import java.io.Serializable;

//the separated logic allows a lazy lookup of this bean and
//avoids that the injection of UserTransaction fails in an unmanaged thread (see DELTASPIKE-917).
@Dependent
public class UserTransactionResolver implements Serializable
{
    protected static final String USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";

    private static final long serialVersionUID = -1432802805095533499L;

    @Resource
    private UserTransaction userTransaction;

    public UserTransaction resolveUserTransaction()
    {
        if (userTransaction != null)
        {
            return userTransaction;
        }

        try
        {
            return JndiUtils.lookup(USER_TRANSACTION_JNDI_NAME, UserTransaction.class);
        }
        catch (Exception e)
        {
            // do nothing it was just a try
            return null;
        }
    }
}
