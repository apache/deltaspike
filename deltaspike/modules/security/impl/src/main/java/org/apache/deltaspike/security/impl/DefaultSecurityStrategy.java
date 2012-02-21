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
package org.apache.deltaspike.security.impl;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.security.api.AccessDecisionState;
import org.apache.deltaspike.security.api.AccessDecisionVoter;
import org.apache.deltaspike.security.api.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.AccessDeniedException;
import org.apache.deltaspike.security.api.Secured;
import org.apache.deltaspike.security.api.SecurityViolation;
import org.apache.deltaspike.security.spi.EditableAccessDecisionVoterContext;
import org.apache.deltaspike.security.spi.SecurityStrategy;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * {@inheritDoc}
 */
@Dependent
public class DefaultSecurityStrategy implements SecurityStrategy
{
    private static final long serialVersionUID = 7992336651801599079L;

    @Inject
    private AccessDecisionVoterContext voterContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(InvocationContext invocationContext) throws Exception
    {
        Secured secured = null;

        List<Annotation> annotatedTypeMetadata = extractMetadata(invocationContext);

        for (Annotation annotation : annotatedTypeMetadata)
        {
            if (Secured.class.isAssignableFrom(annotation.annotationType()))
            {
                secured = (Secured) annotation;
            }
            else if (voterContext instanceof EditableAccessDecisionVoterContext)
            {
                ((EditableAccessDecisionVoterContext) voterContext)
                        .addMetaData(annotation.annotationType().getName(), annotation);
            }
        }

        if (secured != null)
        {
            Class<? extends AccessDecisionVoter>[] voterClasses = secured.value();

            invokeVoters(invocationContext, Arrays.asList(voterClasses));
        }

        return invocationContext.proceed();
    }

    private List<Annotation> extractMetadata(InvocationContext invocationContext)
    {
        List<Annotation> result = new ArrayList<Annotation>();

        Method method = invocationContext.getMethod();

        result.addAll(getAllAnnotations(method.getAnnotations()));
        result.addAll(getAllAnnotations(method.getDeclaringClass().getAnnotations()));

        return result;
    }

    private List<Annotation> getAllAnnotations(Annotation[] annotations)
    {
        List<Annotation> result = new ArrayList<Annotation>();

        String annotationName;
        for (Annotation annotation : annotations)
        {
            annotationName = annotation.annotationType().getName();
            if (annotationName.startsWith("java.") || annotationName.startsWith("javax."))
            {
                continue;
            }

            result.add(annotation);
            result.addAll(getAllAnnotations(annotation.annotationType().getAnnotations()));
        }

        return result;
    }

    /**
     * Helper for invoking the given {@link AccessDecisionVoter}s
     *
     * @param invocationContext    current invocation-context (might be null in case of secured views)
     * @param accessDecisionVoters current access-decision-voters
     */
    private void invokeVoters(InvocationContext invocationContext,
                              List<Class<? extends AccessDecisionVoter>> accessDecisionVoters)
    {
        if (accessDecisionVoters == null)
        {
            return;
        }

        AccessDecisionState voterState = AccessDecisionState.VOTE_IN_PROGRESS;
        try
        {
            if (voterContext instanceof EditableAccessDecisionVoterContext)
            {
                ((EditableAccessDecisionVoterContext) voterContext).setState(voterState);
                ((EditableAccessDecisionVoterContext) voterContext).setSource(invocationContext);
            }

            Set<SecurityViolation> violations;

            AccessDecisionVoter voter;
            for (Class<? extends AccessDecisionVoter> voterClass : accessDecisionVoters)
            {
                voter = BeanProvider.getContextualReference(voterClass, false);

                violations = voter.checkPermission(voterContext);

                if (violations != null && violations.size() > 0)
                {
                    if (voterContext instanceof EditableAccessDecisionVoterContext)
                    {
                        voterState = AccessDecisionState.VIOLATION_FOUND;
                        for (SecurityViolation securityViolation : violations)
                        {
                            ((EditableAccessDecisionVoterContext) voterContext).addViolation(securityViolation);
                        }
                    }
                    throw new AccessDeniedException(violations);
                }
            }
        }
        finally
        {
            if (voterContext instanceof EditableAccessDecisionVoterContext)
            {
                if (AccessDecisionState.VOTE_IN_PROGRESS.equals(voterState))
                {
                    voterState = AccessDecisionState.NO_VIOLATION_FOUND;
                }

                ((EditableAccessDecisionVoterContext) voterContext).setState(voterState);
            }
        }
    }
}
