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


import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

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
 * Annotation a = AnnotationInstanceProvider.of(annotationClass)
 * </pre>
 */
public class AnnotationInstanceProvider implements Annotation, InvocationHandler, Serializable
{
    private static final long serialVersionUID = -2345068201195886173L;
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    private final Class<? extends Annotation> annotationClass;
    private final Map<String, ?> memberValues;

    /**
     * Required to use the result of the factory instead of a default implementation
     * of {@link javax.enterprise.util.AnnotationLiteral}.
     *
     * @param annotationClass class of the target annotation
     */
    private AnnotationInstanceProvider(Class<? extends Annotation> annotationClass, Map<String, ?> memberValues)
    {
        this.annotationClass = annotationClass;
        this.memberValues = memberValues;
    }

    /**
     * Creates an annotation instance for the given annotation class
     *
     * @param annotationClass type of the target annotation
     * @param values          A non-null map of the member values, keys being the name of the members
     * @param <T>             current type
     * @return annotation instance for the given type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T of(Class<T> annotationClass, Map<String, ?> values)
    {
        if (values == null)
        {
            throw new IllegalArgumentException("Map of values must not be null");
        }

        String key = annotationClass.getName() + "_" + values.hashCode();

        return (T) initAnnotation(key, annotationClass, values);
    }

    /**
     * Creates an annotation instance for the given annotation class
     *
     * @param annotationClass type of the target annotation
     * @param <T>             current type
     * @return annotation instance for the given type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T of(Class<T> annotationClass)
    {
        return (T) of(annotationClass, Collections.EMPTY_MAP);
    }

    private static synchronized <T extends Annotation> Annotation initAnnotation(String key,
                                                                                 Class<T> annotationClass,
                                                                                 Map<String, ?> values)
    {
        return (Annotation) Proxy.newProxyInstance(annotationClass.getClassLoader(),
            new Class[]{annotationClass},
            new AnnotationInstanceProvider(annotationClass, values));
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
        else
        {
            if (memberValues.containsKey(method.getName()))
            {
                return memberValues.get(method.getName());
            }
            else // Default cause, probably won't ever happen, unless annotations get actual methods
            {
                return method.getDefaultValue();
            }
        }
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
        Method[] methods = annotationClass.getDeclaredMethods();

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
            if (annotationClass.isInstance(o))
            {
                for (Map.Entry<String, ?> entry : memberValues.entrySet())
                {
                    try
                    {
                        Object oValue = annotationClass.getMethod(entry.getKey(), EMPTY_CLASS_ARRAY)
                                .invoke(o, EMPTY_OBJECT_ARRAY);
                        if (oValue != null && entry.getValue() != null)
                        {
                            if (!oValue.equals(entry.getValue()))
                            {
                                return false;
                            }
                        }
                        else // This may not actually ever happen, unless null is a default for a member
                        {
                            return false;
                        }
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                    catch (InvocationTargetException e)
                    {
                        throw new RuntimeException(e);
                    }
                    catch (NoSuchMethodException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                return true;
            }
            return false;
        }

        AnnotationInstanceProvider that = (AnnotationInstanceProvider) o;

        if (!annotationClass.equals(that.annotationClass))
        {
            return false;
        }

        return memberValues.equals(that.memberValues);
    }

    @Override
    public int hashCode()
    {
        int result = 0;
        Class<? extends Annotation> type = annotationClass;
        for (Method m : type.getDeclaredMethods())
        {
            try
            {
                Object value = invoke(this, m, EMPTY_OBJECT_ARRAY);
                if (value == null)
                {
                    throw new IllegalStateException(String.format("Annotation method %s returned null", m));
                }
                result += hashMember(m.getName(), value);
            }
            catch (RuntimeException ex)
            {
                throw ex;
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }
        return result;
    }

    //besides modularity, this has the advantage of autoboxing primitives:

    /**
     * Helper method for generating a hash code for a member of an annotation.
     *
     * @param name  the name of the member
     * @param value the value of the member
     * @return a hash code for this member
     */
    private int hashMember(String name, Object value)
    {
        int part1 = name.hashCode() * 127;
        if (value.getClass().isArray())
        {
            return part1 ^ arrayMemberHash(value.getClass().getComponentType(), value);
        }
        if (value instanceof Annotation)
        {
            return part1 ^ hashCode((Annotation) value);
        }
        return part1 ^ value.hashCode();
    }

    /**
     * Helper method for generating a hash code for an array.
     *
     * @param componentType the component type of the array
     * @param o             the array
     * @return a hash code for the specified array
     */
    private static int arrayMemberHash(Class<?> componentType, Object o)
    {
        if (componentType.equals(Byte.TYPE))
        {
            return Arrays.hashCode((byte[]) o);
        }
        if (componentType.equals(Short.TYPE))
        {
            return Arrays.hashCode((short[]) o);
        }
        if (componentType.equals(Integer.TYPE))
        {
            return Arrays.hashCode((int[]) o);
        }
        if (componentType.equals(Character.TYPE))
        {
            return Arrays.hashCode((char[]) o);
        }
        if (componentType.equals(Long.TYPE))
        {
            return Arrays.hashCode((long[]) o);
        }
        if (componentType.equals(Float.TYPE))
        {
            return Arrays.hashCode((float[]) o);
        }
        if (componentType.equals(Double.TYPE))
        {
            return Arrays.hashCode((double[]) o);
        }
        if (componentType.equals(Boolean.TYPE))
        {
            return Arrays.hashCode((boolean[]) o);
        }
        return Arrays.hashCode((Object[]) o);
    }

    /**
     * <p>Generate a hash code for the given annotation using the algorithm
     * presented in the {@link Annotation#hashCode()} API docs.</p>
     *
     * @param a the Annotation for a hash code calculation is desired, not
     *          {@code null}
     * @return the calculated hash code
     * @throws RuntimeException      if an {@code Exception} is encountered during
     *                               annotation member access
     * @throws IllegalStateException if an annotation method invocation returns
     *                               {@code null}
     */
    private int hashCode(Annotation a)
    {
        int result = 0;
        Class<? extends Annotation> type = a.annotationType();
        for (Method m : type.getDeclaredMethods())
        {
            try
            {
                Object value = m.invoke(a);
                if (value == null)
                {
                    throw new IllegalStateException(String.format("Annotation method %s returned null", m));
                }
                result += hashMember(m.getName(), value);
            }
            catch (RuntimeException ex)
            {
                throw ex;
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }
        return result;
    }
}
