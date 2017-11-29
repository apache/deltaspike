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

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.security.api.authorization.AccessDecisionState;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.apache.deltaspike.security.api.authorization.Secured;
import org.apache.deltaspike.security.api.authorization.Secures;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.apache.deltaspike.security.impl.util.SecurityUtils;
import org.apache.deltaspike.security.spi.authorization.EditableAccessDecisionVoterContext;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Authorizer implementation for the {@link @Secured} annotation
 */
@Dependent
@SuppressWarnings("UnusedDeclaration")
public class SecuredAnnotationAuthorizer
{
    @Inject
    private AccessDecisionVoterContext voterContext;

    @Inject
    private AccessDeniedExceptionBroadcaster exceptionBroadcaster;

    @Secures
    @Secured({ })
    @SuppressWarnings("UnusedDeclaration")
    public boolean doSecuredCheck(InvocationContext invocationContext) throws Exception
    {
        List<Class<? extends AccessDecisionVoter>> voterClasses = new ArrayList<Class<? extends AccessDecisionVoter>>();

        List<Annotation> annotatedTypeMetadata = extractMetadata(invocationContext);

        for (Annotation annotation : annotatedTypeMetadata)
        {
            if (Secured.class.isAssignableFrom(annotation.annotationType()))
            {
                voterClasses.addAll(Arrays.asList(((Secured) annotation).value()));
            }
            else if (voterContext instanceof EditableAccessDecisionVoterContext)
            {
                ((EditableAccessDecisionVoterContext) voterContext)
                        .addMetaData(annotation.annotationType().getName(), annotation);
            }
        }

        invokeVoters(invocationContext, voterClasses);

        //needed by @SecurityBindingType
        //X TODO check the use-cases for it
        return true;
    }

    protected List<Annotation> extractMetadata(InvocationContext invocationContext)
    {
        List<Annotation> result = new ArrayList<Annotation>();

        Method method = invocationContext.getMethod();

        // some very old EE6 containers have a bug in resolving the target
        // so we fall back on the declaringClass of the method.
        Class<?> targetClass =
                invocationContext.getTarget() != null
                        ? ProxyUtils.getUnproxiedClass(invocationContext.getTarget().getClass())
                        : method.getDeclaringClass();


        result.addAll(SecurityUtils.getAllAnnotations(targetClass.getAnnotations(),
            new HashSet<Integer>()));
        //later on method-level annotations need to overrule class-level annotations -> don't change the order
        result.addAll(SecurityUtils.getAllAnnotations(method.getAnnotations(),
                new HashSet<Integer>()));

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
        if (accessDecisionVoters.isEmpty())
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

                if (violations != null && !violations.isEmpty())
                {
                    if (voterContext instanceof EditableAccessDecisionVoterContext)
                    {
                        voterState = AccessDecisionState.VIOLATION_FOUND;
                        for (SecurityViolation securityViolation : violations)
                        {
                            ((EditableAccessDecisionVoterContext) voterContext).addViolation(securityViolation);
                        }
                    }
                    this.exceptionBroadcaster.broadcastAccessDeniedException(new AccessDeniedException(violations));
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