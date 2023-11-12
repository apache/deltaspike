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
package org.apache.deltaspike.core.impl.future;

import jakarta.annotation.Priority;
import org.apache.deltaspike.core.api.future.Futureable;
import org.apache.deltaspike.core.spi.future.FutureableStrategy;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.io.Serializable;

@Interceptor
@Futureable
@Priority(1000)
public class FutureableInterceptor implements Serializable
{
    @Inject
    private FutureableStrategy futureableStrategy;

    @AroundInvoke
    public Object invoke(final InvocationContext ic) throws Exception
    {
        return futureableStrategy.execute(ic);
    }
}
