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
package org.apache.deltaspike.jsf.impl.security;

import org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

//allows to customize the default handling
@Dependent
public class AccessDeniedExceptionBroadcaster
{
    @Inject
    private BeanManager beanManager;

    public Throwable broadcastAccessDeniedException(AccessDeniedException accessDeniedException)
    {
        ExceptionToCatchEvent exceptionToCatchEvent = new ExceptionToCatchEvent(accessDeniedException);

        try
        {
            this.beanManager.fireEvent(exceptionToCatchEvent);
        }
        catch (AccessDeniedException e)
        {
            //intentionally ignore the exception per default, since we just notify the handlers
            //(like in the other cases with AccessDeniedException)
            //that's different from the normal exception-control handling
            return null;
        }
        return null;
    }
}
