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
package org.apache.deltaspike.test.security.impl.authorization.securityparameterbinding;

import org.apache.deltaspike.security.api.authorization.SecuredReturn;
import org.apache.deltaspike.security.api.authorization.Secures;

import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.InvocationContext;

@ApplicationScoped
public class CustomAuthorizer
{
    @Secures
    @CustomSecurityBinding
    public boolean doSecuredCheck(@MockParamBinding MockObject obj, InvocationContext invocationContext) 
        throws Exception
    {
        return obj.isValue();
    }
    
    @Secures
    @CustomSecurityBinding
    public boolean doSecuredCheck(@MockParamBinding MockObject2 obj)
    {
    	return obj.isValue();
    }
    
    @Secures
    @CustomSecurityBinding
    public boolean doSecuredCheckAfterMethodInvocation(@SecuredReturn MockObject obj)
    {
    	return obj.isValue();
    }

    @Secures
    @CustomSecurityBinding
    public boolean doSecuredCheckAfterMethodInvocationWithVoidMethod(@SecuredReturn Void result) {
        return false;
    }
}
