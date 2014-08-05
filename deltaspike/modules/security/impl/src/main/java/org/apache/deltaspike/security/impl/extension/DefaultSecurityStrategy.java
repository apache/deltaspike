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
package org.apache.deltaspike.security.impl.extension;

import org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.apache.deltaspike.security.impl.authorization.SkipInternalProcessingException;
import org.apache.deltaspike.security.spi.authorization.SecurityStrategy;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * {@inheritDoc}
 */
@Dependent
public class DefaultSecurityStrategy implements SecurityStrategy
{
    private static final long serialVersionUID = 7992336651801599079L;

    @Inject
    private BeanManager beanManager;

    @Inject
    private SecurityExtension securityExtension;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(InvocationContext invocationContext) throws Exception
    {
        Method method = invocationContext.getMethod();

        SecurityMetaDataStorage metaDataStorage = securityExtension.getMetaDataStorage();

        Class targetClass = ProxyUtils.getUnproxiedClass(invocationContext.getTarget().getClass()); //see DELTASPIKE-517

        Set<Authorizer> authorizers = metaDataStorage.getAuthorizers(targetClass, method);

        invokeBeforeMethodInvocationAuthorizers(invocationContext, authorizers);

        Object result = invocationContext.proceed();

        invokeAfterMethodInvocationAuthorizers(invocationContext, authorizers, result);

        return result;
    }

    protected void invokeBeforeMethodInvocationAuthorizers(
        InvocationContext invocationContext, Set<Authorizer> authorizers) throws IllegalAccessException
    {
        try
        {
            for (Authorizer authorizer : authorizers)
            {
                if (authorizer.isBeforeMethodInvocationAuthorizer())
                {
                    authorizer.authorize(invocationContext, null, this.beanManager);
                }
            }
        }
        catch (SkipInternalProcessingException e)
        {
            throw e.getAccessDeniedException();
        }
        catch (AccessDeniedException e)
        {
            RuntimeException exceptionToThrow = handleAccessDeniedException(e);
            if (exceptionToThrow != null)
            {
                throw exceptionToThrow;
            }
        }
    }

    protected void invokeAfterMethodInvocationAuthorizers(InvocationContext invocationContext,
        Set<Authorizer> authorizers, Object result) throws IllegalAccessException
    {
        try
        {
            for (Authorizer authorizer : authorizers)
            {
                if (authorizer.isAfterMethodInvocationAuthorizer())
                {
                    authorizer.authorize(invocationContext, result, this.beanManager);
                }
            }
        }
        catch (AccessDeniedException e)
        {
            RuntimeException exceptionToThrow = handleAccessDeniedException(e);
            if (exceptionToThrow != null)
            {
                throw exceptionToThrow;
            }
        }
    }

    /**
     * <p>Fires a {@link org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent} for the given
     * {@link org.apache.deltaspike.security.api.authorization.AccessDeniedException}.</p>
     * It also allows to change the default handling.
     *
     * @param originalException exception thrown by an authorizer
     * @return the original exception if the default behavior was changed and the exception is unhandled
     */
    protected RuntimeException handleAccessDeniedException(AccessDeniedException originalException)
    {
        ExceptionToCatchEvent exceptionToCatchEvent = new ExceptionToCatchEvent(originalException);
        this.beanManager.fireEvent(exceptionToCatchEvent);
        return originalException;
    }
}