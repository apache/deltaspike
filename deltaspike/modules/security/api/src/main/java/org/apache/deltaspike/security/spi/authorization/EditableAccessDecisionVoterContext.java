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
package org.apache.deltaspike.security.spi.authorization;

import org.apache.deltaspike.security.api.authorization.AccessDecisionState;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;

/**
 * Interface which allows to provide a custom {@link AccessDecisionVoterContext} implementation
 */
public interface EditableAccessDecisionVoterContext extends AccessDecisionVoterContext
{
    /**
     * Allows to add custom meta-data. The default security strategy adds custom annotations of the intercepted method
     * as well as class-level annotations. (Currently inherited annotations aren't supported)
     * @param key key for the meta-data
     * @param metaData meta-data which should be added
     */
    void addMetaData(String key, Object metaData);

    /**
     * Updates the state of the context
     * @param accessDecisionVoterState current state
     */
    void setState(AccessDecisionState accessDecisionVoterState);

    /**
     * TODO review it (this method is new)
     * @param source e.g. the invocation-context
     */
    void setSource(Object source);

    /**
     * Adds a new {@link SecurityViolation} to the context
     * @param securityViolation security-violation which should be added
     */
    void addViolation(SecurityViolation securityViolation);
}
