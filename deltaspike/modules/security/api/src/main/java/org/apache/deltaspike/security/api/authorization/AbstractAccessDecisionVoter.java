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

import java.util.HashSet;
import java.util.Set;

/**
 * Base implementation which provides helper methods.
 */
public abstract class AbstractAccessDecisionVoter extends AbstractDecisionVoter implements AccessDecisionVoter
{
    private static final long serialVersionUID = -9145021044568668681L;

    /**
     * It should be final - but proxy-libs won't support it.
     */
    @Override
    public Set<SecurityViolation> checkPermission(AccessDecisionVoterContext accessDecisionVoterContext)
    {
        Set<SecurityViolation> result = new HashSet<SecurityViolation>();

        checkPermission(accessDecisionVoterContext, result);

        return result;
    }

    /**
     * Allows an easier implementation in combination with {@link #newSecurityViolation(String)}.
     *
     * @param accessDecisionVoterContext current accessDecisionVoterContext
     * @param violations set for adding violations
     */
    protected abstract void checkPermission(AccessDecisionVoterContext accessDecisionVoterContext,
                                            Set<SecurityViolation> violations);
}