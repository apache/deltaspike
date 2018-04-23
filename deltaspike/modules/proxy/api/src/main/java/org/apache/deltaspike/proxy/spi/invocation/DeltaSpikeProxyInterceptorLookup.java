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
package org.apache.deltaspike.proxy.spi.invocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;

/**
 * Utility which stores the information about configured interceptors for each method.
 */
@ApplicationScoped
public class DeltaSpikeProxyInterceptorLookup
{
    private final Map<Method, List<Interceptor<?>>> cache = new HashMap<>();
    
    public List<Interceptor<?>> lookup(Object instance, Method method)
    {
        List<Interceptor<?>> interceptors = cache.get(method);
        
        if (interceptors == null)
        {
            interceptors = resolveInterceptors(instance, method);
            cache.put(method, interceptors);
        }
        
        return interceptors;
    }
    
    private List<Interceptor<?>> resolveInterceptors(Object instance, Method method)
    {
        BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
        
        Annotation[] interceptorBindings = extractInterceptorBindings(beanManager, instance, method);
        if (interceptorBindings.length > 0)
        {
            return beanManager.resolveInterceptors(InterceptionType.AROUND_INVOKE, interceptorBindings);
        }

        return new ArrayList<>();
    }

    private Annotation[] extractInterceptorBindings(BeanManager beanManager, Object instance, Method method)
    {
        ArrayList<Annotation> bindings = new ArrayList<>();

        addInterceptorBindings(beanManager, bindings, instance.getClass().getDeclaredAnnotations());
        addInterceptorBindings(beanManager, bindings, method.getDeclaredAnnotations());

        return bindings.toArray(new Annotation[bindings.size()]);
    }
    
    private void addInterceptorBindings(BeanManager beanManager, ArrayList<Annotation> bindings,
            Annotation[] declaredAnnotations)
    {
        for (Annotation annotation : declaredAnnotations)
        {
            if (bindings.contains(annotation))
            {
                continue;
            }
            
            Class<? extends Annotation> annotationType = annotation.annotationType();
            
            if (beanManager.isInterceptorBinding(annotationType))
            {
                bindings.add(annotation);
            }
            
            if (beanManager.isStereotype(annotationType))
            {
                for (Annotation subAnnotation : annotationType.getDeclaredAnnotations())
                {                    
                    if (bindings.contains(subAnnotation))
                    {
                        continue;
                    }

                    if (beanManager.isInterceptorBinding(subAnnotation.annotationType()))
                    {
                        bindings.add(subAnnotation);
                    }  
                }
            }
        }
    }
}
