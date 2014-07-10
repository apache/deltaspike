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
package org.apache.deltaspike.security.impl.authorization;

import org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

//this broadcaster just allows to change the default behavior (if needed)
//needed because it needs to be possible to 'consume' exceptions of type AccessDeniedException.
//instead of ignoring the result of exception-control and throwing them in any case (like we have to do it per default).
@Dependent
public class AccessDeniedExceptionBroadcaster
{
    @Inject
    private BeanManager beanManager;

    public void broadcastAccessDeniedException(AccessDeniedException accessDeniedException)
    {
        ExceptionToCatchEvent exceptionToCatchEvent = new ExceptionToCatchEvent(accessDeniedException);

        try
        {
            this.beanManager.fireEvent(exceptionToCatchEvent);
        }
        catch (AccessDeniedException e)
        {
            throw new SkipInternalProcessingException(accessDeniedException);
        }
        //we have to throw it in any case to support "observers" for AccessDeniedException (see DELTASPIKE-636)
        //however, currently we can't do it based on the exception-control api (see DELTASPIKE-638)
        throw new SkipInternalProcessingException(accessDeniedException);
    }
}
