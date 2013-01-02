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

import org.apache.deltaspike.security.api.authorization.SecurityViolation;

import java.util.Set;

/**
 * Allows to handle custom implementations of {@link SecurityViolation}
 */
public interface SecurityViolationHandler
{
    /**
     * Instead of adding the violations as message for the user, it's possible to implement a custom behaviour
     * (e.g. something like an InternalViolation which won't get added)
     * @param securityViolations current violations
     */
    void processSecurityViolations(Set<SecurityViolation> securityViolations);
}
