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
package org.apache.deltaspike.core.impl.monitoring;

import org.apache.deltaspike.core.api.monitoring.InvocationMonitored;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Interceptor
@InvocationMonitored
public class InvocationMonitorInterceptor implements Serializable
{
    private static final Logger logger = Logger.getLogger(InvocationMonitorInterceptor.class.getName());

    @Inject
    private RequestInvocationCounter requestInvocationCounter;

    @AroundInvoke
    public Object track(InvocationContext ic) throws Exception
    {
        long start = System.nanoTime();
        Object retVal = ic.proceed();
        long end = System.nanoTime();
        try
        {
            requestInvocationCounter.count(ic.getTarget().getClass().getName(), ic.getMethod().getName(), end - start);
        }
        catch (ContextNotActiveException cnae)
        {
            logger.log(Level.FINE, "could not monitor invocatino to {} due to RequestContext not being active",
                ic.getMethod().toString());
        }

        return retVal;
    }

}
