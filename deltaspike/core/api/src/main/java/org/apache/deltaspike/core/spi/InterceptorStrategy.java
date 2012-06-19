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
package org.apache.deltaspike.core.spi;

import javax.interceptor.InvocationContext;
import java.io.Serializable;

/**
 * Base interface for all interceptor strategies which allow to provide
 * custom implementations for DeltaSpike interceptors.
 */
public interface InterceptorStrategy extends Serializable
{
    /**
     * Method which will be invoked by the interceptor method annotated with {@link javax.interceptor.AroundInvoke}
     * @param invocationContext current invocation-context
     * @return result of the intercepted method
     * @throws Exception exception which might be thrown by the intercepted method
     */
    Object execute(InvocationContext invocationContext) throws Exception;
}
