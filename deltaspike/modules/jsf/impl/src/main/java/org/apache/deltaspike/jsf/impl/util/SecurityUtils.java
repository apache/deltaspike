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
package org.apache.deltaspike.jsf.impl.util;

import org.apache.deltaspike.core.api.config.view.DefaultErrorView;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.apache.deltaspike.security.api.authorization.AccessDecisionState;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.apache.deltaspike.security.api.authorization.ErrorViewAwareAccessDeniedException;
import org.apache.deltaspike.security.api.authorization.Secured;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.apache.deltaspike.security.spi.authorization.EditableAccessDecisionVoterContext;
import org.apache.deltaspike.security.spi.authorization.SecurityViolationHandler;

import javax.enterprise.inject.Typed;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Typed()
public abstract class SecurityUtils
{
    public static void invokeVoters(EditableAccessDecisionVoterContext accessDecisionVoterContext,
                                    ConfigDescriptor<?> viewConfigDescriptor)
    {
        if (viewConfigDescriptor == null)
        {
            return;
        }

        List<Secured> securedMetaData = viewConfigDescriptor.getMetaData(Secured.class);

        if (securedMetaData.isEmpty())
        {
            return;
        }

        accessDecisionVoterContext.addMetaData(ViewConfig.class.getName(), viewConfigDescriptor.getConfigClass());
        for (Annotation viewMetaData : viewConfigDescriptor.getMetaData())
        {
            if (!viewMetaData.annotationType().equals(Secured.class))
            {
                accessDecisionVoterContext.addMetaData(viewMetaData.annotationType().getName(), viewMetaData);
            }
        }

        Secured.Descriptor securedDescriptor = viewConfigDescriptor
                .getExecutableCallbackDescriptor(Secured.class, Secured.Descriptor.class);

        AccessDecisionState voterState = AccessDecisionState.VOTE_IN_PROGRESS;
        try
        {
            accessDecisionVoterContext.setState(voterState);

            List<Set<SecurityViolation>> violations = securedDescriptor.execute(accessDecisionVoterContext);
            Set<SecurityViolation> allViolations = createViolationResult(violations);

            if (!allViolations.isEmpty())
            {
                voterState = AccessDecisionState.VIOLATION_FOUND;
                for (SecurityViolation violation : allViolations)
                {
                    accessDecisionVoterContext.addViolation(violation);
                }

                Class<? extends ViewConfig> errorView = securedMetaData.iterator().next().errorView();
                throw new ErrorViewAwareAccessDeniedException(allViolations, errorView);
            }
        }
        finally
        {
            if (AccessDecisionState.VOTE_IN_PROGRESS.equals(voterState))
            {
                voterState = AccessDecisionState.NO_VIOLATION_FOUND;
            }

            accessDecisionVoterContext.setState(voterState);
        }
    }

    private static Set<SecurityViolation> createViolationResult(List<Set<SecurityViolation>> violations)
    {
        if (violations == null || violations.isEmpty())
        {
            return Collections.emptySet();
        }

        Set<SecurityViolation> result = new HashSet<SecurityViolation>();

        for (Set<SecurityViolation> securityViolationSet : violations)
        {
            result.addAll(securityViolationSet);
        }
        return result;
    }

    /**
     * Processes a security violation without triggering the navigation to the error page
     *
     * @param exception current exception
     */
    public static void handleSecurityViolationWithoutNavigation(RuntimeException exception)
    {
        tryToHandleSecurityViolation(exception, false);
    }

    /**
     * Processes a security violation including the navigation to the error page
     *
     * @param exception current exception
     */
    public static void tryToHandleSecurityViolation(RuntimeException exception)
    {
        tryToHandleSecurityViolation(exception, true);
    }

    private static void tryToHandleSecurityViolation(RuntimeException runtimeException,
                                                     boolean allowNavigation)
    {
        ErrorViewAwareAccessDeniedException exception = extractException(runtimeException);

        if (exception == null)
        {
            throw runtimeException;
        }

        Class<? extends ViewConfig> errorView = null;

        Class<? extends ViewConfig> inlineErrorView = exception.getErrorView();

        if (inlineErrorView != null && !DefaultErrorView.class.getName().equals(inlineErrorView.getName()))
        {
            errorView = inlineErrorView;
        }

        if (errorView == null)
        {
            ViewConfigResolver viewConfigResolver = BeanProvider.getContextualReference(ViewConfigResolver.class);
            ViewConfigDescriptor errorPageDescriptor = viewConfigResolver.getDefaultErrorViewConfigDescriptor();

            if (errorPageDescriptor != null)
            {
                errorView = errorPageDescriptor.getConfigClass();
            }
        }

        if (errorView == null && allowNavigation)
        {
            throw exception;
        }

        processApplicationSecurityException(exception, errorView, allowNavigation);
    }

    private static ErrorViewAwareAccessDeniedException extractException(Throwable exception)
    {
        if (exception == null)
        {
            return null;
        }

        if (exception instanceof ErrorViewAwareAccessDeniedException)
        {
            return (ErrorViewAwareAccessDeniedException) exception;
        }

        return extractException(exception.getCause());
    }

    private static void processApplicationSecurityException(AccessDeniedException exception,
                                                            Class<? extends ViewConfig> errorView,
                                                            boolean allowNavigation)
    {
        SecurityViolationHandler securityViolationHandler =
                BeanProvider.getContextualReference(SecurityViolationHandler.class, true);

        if (securityViolationHandler != null)
        {
            //optional (custom handler) - allows to handle custom implementations of SecurityViolation
            securityViolationHandler.processSecurityViolations(exception.getViolations());
        }
        else
        {
            addViolationsAsMessage(exception.getViolations());
        }

        if (allowNavigation)
        {
            BeanProvider.getContextualReference(ViewNavigationHandler.class).navigateTo(errorView);
        }
    }

    private static void addViolationsAsMessage(Set<SecurityViolation> violations)
    {
        String message;
        for (SecurityViolation violation : violations)
        {
            //TODO discuss it (with CODI handling such messages was easier)
            message = violation.getReason();

            if (!isMessageAddedAlready(message))
            {
                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message);
                FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            }
        }
    }

    private static boolean isMessageAddedAlready(String message)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext == null || message == null)
        {
            return false;
        }

        List<FacesMessage> existingMessages = facesContext.getMessageList();

        if (existingMessages == null)
        {
            return false;
        }

        for (FacesMessage facesMessage : existingMessages)
        {
            if (message.equals(facesMessage.getSummary()))
            {
                return true;
            }
        }
        return false;
    }
}
