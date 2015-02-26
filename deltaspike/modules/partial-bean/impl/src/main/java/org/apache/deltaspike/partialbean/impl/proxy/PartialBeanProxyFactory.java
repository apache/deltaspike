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
package org.apache.deltaspike.partialbean.impl.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.enterprise.inject.Typed;
import org.apache.deltaspike.core.util.ClassUtils;

@Typed
public abstract class PartialBeanProxyFactory
{
    private static final String CLASSNAME_SUFFIX = "$$DSPartialBeanProxy";

    private PartialBeanProxyFactory()
    {
        // prevent instantiation
    }

    public static <T> Class<T> getProxyClass(Class<T> targetClass,
            Class<? extends InvocationHandler> invocationHandlerClass)
    {
        Class<T> proxyClass = ClassUtils.tryToLoadClassForName(constructProxyClassName(targetClass), targetClass);
        if (proxyClass == null)
        {
            proxyClass = createProxyClass(targetClass.getClassLoader(), targetClass, invocationHandlerClass);
        }

        return proxyClass;
    }

    private static synchronized <T> Class<T> createProxyClass(ClassLoader classLoader, Class<T> targetClass,
            Class<? extends InvocationHandler> invocationHandlerClass)
    {
        Class<T> proxyClass = ClassUtils.tryToLoadClassForName(constructProxyClassName(targetClass), targetClass);
        if (proxyClass == null)
        {
            ArrayList<Method> redirectMethods = new ArrayList<Method>();
            ArrayList<Method> interceptionMethods = new ArrayList<Method>();
            collectMethods(targetClass, redirectMethods, interceptionMethods);

            proxyClass = AsmProxyClassGenerator.generateProxyClass(classLoader,
                    targetClass,
                    invocationHandlerClass,
                    CLASSNAME_SUFFIX,
                    redirectMethods.toArray(new Method[redirectMethods.size()]),
                    interceptionMethods.toArray(new Method[interceptionMethods.size()]));
        }

        return proxyClass;
    }

    private static String constructProxyClassName(Class<?> clazz)
    {
        return clazz.getCanonicalName() + CLASSNAME_SUFFIX;
    }

    /**
     * Checks if the given class is DS proxy class.
     *
     * @param clazz
     * @return
     */
    public static boolean isProxyClass(Class<?> clazz)
    {
        return clazz.getName().endsWith(CLASSNAME_SUFFIX);
    }

    private static void collectMethods(Class<?> clazz,
            ArrayList<Method> redirectMethods,
            ArrayList<Method> interceptionMethods)
    {
        List<Method> methods = new ArrayList<Method>();
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

        Iterator<Method> it = methods.iterator();
        while (it.hasNext())
        {
            Method method = it.next();

            if (Modifier.isAbstract(method.getModifiers()))
            {
                redirectMethods.add(method);
            }
            else if (Modifier.isPublic(method.getModifiers()) && !Modifier.isFinal(method.getModifiers()))
            {
                interceptionMethods.add(method);
            }
        }

    }

    private static boolean ignoreMethod(Method method, List<Method> methods)
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

    private static boolean hasSameSignature(Method a, Method b)
    {
        return a.getName().equals(b.getName())
                && a.getReturnType().equals(b.getReturnType())
                && Arrays.equals(a.getParameterTypes(), b.getParameterTypes());
    }
}