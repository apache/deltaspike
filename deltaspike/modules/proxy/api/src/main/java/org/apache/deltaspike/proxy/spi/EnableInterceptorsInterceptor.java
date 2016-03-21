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
package org.apache.deltaspike.proxy.spi;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.apache.deltaspike.proxy.api.EnableInterceptors;

@Interceptor
@EnableInterceptors
public class EnableInterceptorsInterceptor implements Serializable
{
    @Inject
    private BeanManager beanManager;
    
    @AroundInvoke
    public Object wrapBeanCandidate(InvocationContext invocationContext) throws Exception
    {
        Object beanCandidate = invocationContext.proceed();
        
        if (beanCandidate == null)
        {
            throw new IllegalStateException("Can not apply "
                    + EnableInterceptors.class.getSimpleName()
                    + " on a null instance!");
        }

        Class proxyClass = EnableInterceptorsProxyFactory.getInstance().getProxyClass(beanManager,
                beanCandidate.getClass(), EnableInterceptorsDelegate.class);
                
        Constructor constructor = proxyClass.getConstructor(EnableInterceptorsDelegate.class);
        return constructor.newInstance(new EnableInterceptorsDelegate(beanCandidate));
    }
}
