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
package org.apache.deltaspike.proxy.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.interceptor.InterceptorBinding;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ServiceUtils;
import org.apache.deltaspike.proxy.spi.ProxyClassGenerator;

public abstract class DeltaSpikeProxyFactory
{
    private static final String SUPER_ACCESSOR_METHOD_SUFFIX = "$super";
    
    public <T> Class<T> getProxyClass(Class<T> targetClass,
            Class<? extends InvocationHandler> delegateInvocationHandlerClass)
    {
        // check if a proxy is already defined for this class
        Class<T> proxyClass = ClassUtils.tryToLoadClassForName(constructProxyClassName(targetClass), targetClass);
        if (proxyClass == null)
        {
            proxyClass = createProxyClass(targetClass.getClassLoader(), targetClass, delegateInvocationHandlerClass);
        }

        return proxyClass;
    }

    private synchronized <T> Class<T> createProxyClass(ClassLoader classLoader, Class<T> targetClass,
            Class<? extends InvocationHandler> delegateInvocationHandlerClass)
    {
        Class<T> proxyClass = ClassUtils.tryToLoadClassForName(constructProxyClassName(targetClass), targetClass);
        if (proxyClass == null)
        {
            ArrayList<Method> allMethods = collectAllMethods(targetClass);
            ArrayList<Method> interceptMethods = filterInterceptMethods(targetClass, allMethods);
            ArrayList<Method> delegateMethods = getDelegateMethods(targetClass, allMethods);

            // check if a interceptor is defined on class level. if not, skip interceptor methods
            if (delegateMethods != null
                    && interceptMethods.size() > 0
                    && !containsInterceptorBinding(targetClass.getDeclaredAnnotations()))
            {
                // loop every method and check if a interceptor is defined on the method -> otherwise don't overwrite
                // interceptMethods
                Iterator<Method> iterator = interceptMethods.iterator();
                while (iterator.hasNext())
                {
                    Method method = iterator.next();
                    if (!containsInterceptorBinding(method.getDeclaredAnnotations()))
                    {
                        iterator.remove();
                    }
                }
            }

            List<ProxyClassGenerator> proxyClassGeneratorList =
                ServiceUtils.loadServiceImplementations(ProxyClassGenerator.class);

            if (proxyClassGeneratorList.size() != 1)
            {
                throw new IllegalStateException(proxyClassGeneratorList.size()
                    + " implementations of " + ProxyClassGenerator.class.getName()
                    + " found. It's just allowed to use one implementation.");
            }

            proxyClass = proxyClassGeneratorList.iterator().next().generateProxyClass(classLoader,
                    targetClass,
                    delegateInvocationHandlerClass,
                    getProxyClassSuffix(),
                    SUPER_ACCESSOR_METHOD_SUFFIX,
                    getAdditionalInterfacesToImplement(targetClass),
                    delegateMethods == null ? new Method[0]
                            : delegateMethods.toArray(new Method[delegateMethods.size()]),
                    interceptMethods == null ? new Method[0]
                            : interceptMethods.toArray(new Method[interceptMethods.size()]));
        }

        return proxyClass;
    }
    
    // TODO stereotypes
    protected boolean containsInterceptorBinding(Annotation[] annotations)
    {
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType().isAnnotationPresent(InterceptorBinding.class))
            {
                return true;
            }
        }
        
        return false;
    }
        
    protected String constructProxyClassName(Class<?> clazz)
    {
        return clazz.getName() + getProxyClassSuffix();
    }

    protected static String constructSuperAccessorMethodName(Method method)
    {
        return method.getName() + SUPER_ACCESSOR_METHOD_SUFFIX;
    }
    
    public static Method getSuperAccessorMethod(Object proxy, Method method) throws NoSuchMethodException
    {
        return proxy.getClass().getMethod(
                constructSuperAccessorMethodName(method),
                method.getParameterTypes());
    }
    
    /**
     * Checks if the given class is DS proxy class.
     *
     * @param clazz
     * @return
     */
    public boolean isProxyClass(Class<?> clazz)
    {
        return clazz.getName().endsWith(getProxyClassSuffix());
    }

    protected boolean hasSameSignature(Method a, Method b)
    {
        return a.getName().equals(b.getName())
                && a.getReturnType().equals(b.getReturnType())
                && Arrays.equals(a.getParameterTypes(), b.getParameterTypes());
    }

    protected boolean ignoreMethod(Method method, List<Method> methods)
    {
        // we have no interest in generics bridge methods
        if (method.isBridge())
        {
            return true;
        }

        // we do not proxy finalize()
        if ("finalize".equals(method.getName()))
        {
            return true;
        }

        // same method...
        if (methods.contains(method))
        {
            return true;
        }

        // check if a method with the same signature is already available
        for (Method currentMethod : methods)
        {
            if (hasSameSignature(currentMethod, method))
            {
                return true;
            }
        }

        return false;
    }
    
    protected ArrayList<Method> collectAllMethods(Class<?> clazz)
    {
        ArrayList<Method> methods = new ArrayList<Method>();
        for (Method method : clazz.getDeclaredMethods())
        {
            if (!ignoreMethod(method, methods))
            {
                methods.add(method);
            }
        }
        for (Method method : clazz.getMethods())
        {
            if (!ignoreMethod(method, methods))
            {
                methods.add(method);
            }
        }

        // collect methods from abstract super classes...
        Class currentSuperClass = clazz.getSuperclass();
        while (currentSuperClass != null)
        {
            if (Modifier.isAbstract(currentSuperClass.getModifiers()))
            {
                for (Method method : currentSuperClass.getDeclaredMethods())
                {
                    if (!ignoreMethod(method, methods))
                    {
                        methods.add(method);
                    }
                }
                for (Method method : currentSuperClass.getMethods())
                {
                    if (!ignoreMethod(method, methods))
                    {
                        methods.add(method);
                    }
                }
            }
            currentSuperClass = currentSuperClass.getSuperclass();
        }

        // sort out somewhere implemented abstract methods
        Class currentClass = clazz;
        while (currentClass != null)
        {
            Iterator<Method> methodIterator = methods.iterator();
            while (methodIterator.hasNext())
            {
                Method method = methodIterator.next();
                if (Modifier.isAbstract(method.getModifiers()))
                {
                    try
                    {
                        Method foundMethod = currentClass.getMethod(method.getName(), method.getParameterTypes());
                        // if method is implementent in the current class -> remove it
                        if (foundMethod != null && !Modifier.isAbstract(foundMethod.getModifiers()))
                        {
                            methodIterator.remove();
                        }
                    }
                    catch (Exception e)
                    {
                        // ignore...
                    }
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return methods;
    }
    
    protected ArrayList<Method> filterInterceptMethods(Class<?> targetClass, ArrayList<Method> allMethods)
    {
        ArrayList<Method> methods = new ArrayList<Method>();
        
        Iterator<Method> it = allMethods.iterator();
        while (it.hasNext())
        {
            Method method = it.next();

            if (Modifier.isPublic(method.getModifiers())
                    && !Modifier.isFinal(method.getModifiers())
                    && !Modifier.isAbstract(method.getModifiers()))
            {
                methods.add(method);
            }
        }
        
        return methods;
    }
    
    protected Class<?>[] getAdditionalInterfacesToImplement(Class<?> targetClass)
    {
        return null;
    }
    
    protected abstract ArrayList<Method> getDelegateMethods(Class<?> targetClass, ArrayList<Method> allMethods);
    
    protected abstract String getProxyClassSuffix();
}

