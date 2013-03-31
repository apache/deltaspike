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
package org.apache.deltaspike.security.api.authorization;

import org.apache.deltaspike.core.api.config.view.metadata.DefaultCallback;

import java.io.Serializable;
import java.util.Set;

/**
 * Interface for implementing concrete voters.
 * A voter has to add an instance of
 * {@link SecurityViolation} to the given result-set,
 * if a restriction is detected.<p/>
 * A voter has to be used in combination with
 * {@link Secured}.<p/>
 * A voter can use every scope which is active. It's recommended to use
 * {@link javax.enterprise.context.ApplicationScoped} for stateless voters and e.g.
 * {@link javax.enterprise.context.RequestScoped} otherwise.
 */
public interface AccessDecisionVoter extends Serializable
{
    /**
     * Checks the permission for the given {@link javax.interceptor.InvocationContext}.
     * If a violation is detected, it should be added to a set which gets returned by the method.
     *
     * @param accessDecisionVoterContext current access-decision-voter-context
     * @return a set which contains violations which have been detected
     */
    @DefaultCallback
    Set<SecurityViolation> checkPermission(AccessDecisionVoterContext accessDecisionVoterContext);
}
