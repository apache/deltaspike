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

package org.apache.deltaspike.core.util.metadata;


import org.apache.deltaspike.core.util.ClassUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>A small helper class to create an Annotation instance of the given annotation class
 * via {@link java.lang.reflect.Proxy}. The annotation literal gets filled with the default values.</p>
 * <p/>
 * <p>This class can be used to dynamically create Annotations which can be usd in AnnotatedTyp.
 * This is e.g. the case if you configure an annotation via properties or XML file. In those cases you
 * cannot use {@link javax.enterprise.util.AnnotationLiteral} because the type is not known at compile time.</p>
 * <p>usage:</p>
 * <pre>
 * String annotationClassName = ...;
 * Class<? extends annotation> annotationClass =
 *     (Class<? extends Annotation>) ClassUtils.getClassLoader(null).loadClass(annotationClassName);
 * Annotation a = DefaultAnnotation.of(annotationClass)
 * </pre>
 */
public class AnnotationInstanceProvider implements Annotation, InvocationHandler, Serializable
{
    private static final long serialVersionUID = -2345068201195886173L;
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    // NOTE that this cache needs to be a WeakHashMap in order to prevent a memory leak
    // (the garbage collector should be able to remove the ClassLoader).
    private static volatile Map<ClassLoader, Map<String, Annotation>> annotationCache
            = new WeakHashMap<ClassLoader, Map<String, Annotation>>();

    private Class<? extends Annotation> annotationClass;


    /**
     * Required to use the result of the factory instead of a default implementation
     * of {@link javax.enterprise.util.AnnotationLiteral}.
     *
     * @param annotationClass class of the target annotation
     */
    private AnnotationInstanceProvider(Class<? extends Annotation> annotationClass)
    {
        this.annotationClass = annotationClass;
    }


    /**
     * Creates an annotation instance for the given annotation class
     *
     * @param annotationClass type of the target annotation
     * @param <T>             current type
     * @return annotation instance for the given type
     */
    public static <T extends Annotation> T of(Class<T> annotationClass)
    {
        String key = annotationClass.getName();

        Map<String, Annotation> cache = getAnnotationCache();

        Annotation annotation = cache.get(key);

        if (annotation == null)
        {
            annotation = initAnnotation(key, annotationClass, cache);
        }

        return (T) annotation;
    }

    private static synchronized <T extends Annotation> Annotation initAnnotation(String key,
                                                                                 Class<T> annotationClass,
                                                                                 Map<String, Annotation> cache)
    {
        Annotation annotation = cache.get(key);

        // switch into paranoia mode
        if (annotation == null)
        {
            annotation = (Annotation) Proxy.newProxyInstance(annotationClass.getClassLoader(),
                    new Class[]{annotationClass},
                    new AnnotationInstanceProvider(annotationClass));

            cache.put(key, annotation);
        }

        return annotation;
    }

    private static Map<String, Annotation> getAnnotationCache()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);
        Map<String, Annotation> cache = annotationCache.get(classLoader);

        if (cache == null)
        {
            cache = init(classLoader);
        }

        return cache;
    }

    private static synchronized Map<String, Annotation> init(ClassLoader classLoader)
    {
        // switch into paranoia mode
        Map<String, Annotation> cache = annotationCache.get(classLoader);
        if (cache == null)
        {
            cache = new ConcurrentHashMap<String, Annotation>();
            annotationCache.put(classLoader, cache);
        }
        return cache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception
    {
        if ("hashCode".equals(method.getName()))
        {
            return hashCode();
        }
        else if ("equals".equals(method.getName()))
        {
            if (Proxy.isProxyClass(args[0].getClass()))
            {
                if (Proxy.getInvocationHandler(args[0]) instanceof AnnotationInstanceProvider)
                {
                    return equals(Proxy.getInvocationHandler(args[0]));
                }
            }
            return equals(args[0]);
        }
        else if ("annotationType".equals(method.getName()))
        {
            return annotationType();
        }
        else if ("toString".equals(method.getName()))
        {
            return toString();
        }

        return method.getDefaultValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Annotation> annotationType()
    {
        return annotationClass;
    }

    /**
     * Copied from Apache OWB (javax.enterprise.util.AnnotationLiteral#toString())
     * with minor changes.
     *
     * @return the current state of the annotation as string
     */
    @Override
    public String toString()
    {
        Method[] methods = this.annotationClass.getDeclaredMethods();

        StringBuilder sb = new StringBuilder("@" + annotationType().getName() + "(");
        int length = methods.length;

        for (int i = 0; i < length; i++)
        {
            // Member name
            sb.append(methods[i].getName()).append("=");

            // Member value
            Object memberValue;
            try
            {
                memberValue = invoke(this, methods[i], EMPTY_OBJECT_ARRAY);
            }
            catch (Exception e)
            {
                memberValue = "";
            }
            sb.append(memberValue);

            if (i < length - 1)
            {
                sb.append(",");
            }
        }

        sb.append(")");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AnnotationInstanceProvider))
        {
            return false;
        }

        AnnotationInstanceProvider that = (AnnotationInstanceProvider) o;

        if (!annotationClass.equals(that.annotationClass))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return annotationClass.hashCode();
    }
}
