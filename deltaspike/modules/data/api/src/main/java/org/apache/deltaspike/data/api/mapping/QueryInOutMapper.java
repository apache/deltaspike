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
package org.apache.deltaspike.data.api.mapping;

import java.util.List;

/**
 * Handles concrete mapping of query results and
 * query input parameters.
 */
public interface QueryInOutMapper<E>
{

    /**
     * Map a single result query.
     * @param result        The query result to map.
     * @return              The mapped result object.
     */
    Object mapResult(E result);

    /**
     * Map a query result list.
     * @param result        The query result list to map.
     * @return              The mapped result. Does not have to be a collection.
     */
    Object mapResultList(List<E> result);

    /**
     * Check if this mapper handles a specific input parameter.
     * @param parameter     The parameter candidate for mapping.
     * @return              {@code true} if the mapper handles the parameter.
     */
    boolean mapsParameter(Object parameter);

    /**
     * Map a query parameter.
     * @param parameter     The parameter to map. It can be assumed that the
     *                      {@link #mapsParameter(Object)} method has been
     *                      called before with this parameter.
     * @return              The mapped result.
     */
    Object mapParameter(Object parameter);

}
