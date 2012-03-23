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

import java.util.List;
import java.util.Map;

/**
 * Optional context which allows to get the current state as well as the results of the security check.
 * (Optional because it requires a useful scope which depends on the environment.)
 */
public interface AccessDecisionVoterContext
{
    /**
     * Exposes the current state
     * @return current state
     */
    AccessDecisionState getState();

    /**
     * Exposes the found violations
     * @return found violations
     */
    List<SecurityViolation> getViolations();

    /**
     * TODO review it (this method is new)
     * Exposes the source e.g. {@link javax.interceptor.InvocationContext}
     * @return the source which triggered the
     */
    <T> T getSource();

    /**
     * Exposes the found meta-data
     * @return found meta-data
     */
    Map<String, Object> getMetaData();

    /**
     * Exposes meta-data for the given key
     * @param key meta-data key
     * @param targetType target type
     * @param <T> target type
     * @return meta-data for the given key or null if there is no value for the given key
     */
    <T> T getMetaDataFor(String key, Class<T> targetType);
}
