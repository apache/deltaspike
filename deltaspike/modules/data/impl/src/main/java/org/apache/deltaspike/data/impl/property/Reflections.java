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
package org.apache.deltaspike.data.impl.property;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for working with JDK Reflection and also CDI's
 * {@link javax.enterprise.inject.spi.Annotated} metadata.
 */
public class Reflections
{
    private Reflections()
    {
    }

    /**
     * <p>
     * Perform a runtime cast. Similar to {@link Class#cast(Object)}, but useful when you do not have a {@link Class}
     * object for type you wish to cast to.
     * </p>
     * <p/>
     * <p>
     * {@link Class#cast(Object)} should be used if possible
     * </p>
     *
     * @param <T>
     *            the type to cast to
     * @param obj
     *            the object to perform the cast on
     * @return the casted object
     * @throws ClassCastException
     *             if the type T is not a subtype of the object
     * @see Class#cast(Object)
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj)
    {
        return (T) obj;
    }

    /**
     * Determine if a method exists in a specified class hierarchy
     *
     * @param clazz
     *            The class to search
     * @param name
     *            The name of the method
     * @return true if a method is found, otherwise false
     */
    public static boolean methodExists(Class<?> clazz, String name)
    {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass())
        {
            for (Method m : c.getDeclaredMethods())
            {
                if (m.getName().equals(name))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get all the declared methods on the class hierarchy. This <b>will</b> return overridden methods.
     *
     * @param clazz
     *            The class to search
     * @return the set of all declared methods or an empty set if there are none
     */
    public static Set<Method> getAllDeclaredMethods(Class<?> clazz)
    {
        HashSet<Method> methods = new HashSet<Method>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass())
        {
            for (Method a : c.getDeclaredMethods())
            {
                methods.add(a);
            }
        }
        return methods;
    }

    /**
     * Search the class hierarchy for a method with the given name and arguments. Will return the nearest match,
     * starting with the class specified and searching up the hierarchy.
     *
     * @param clazz
     *            The class to search
     * @param name
     *            The name of the method to search for
     * @param args
     *            The arguments of the method to search for
     * @return The method found, or null if no method is found
     */
    public static Method findDeclaredMethod(Class<?> clazz, String name, Class<?>... args)
    {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass())
        {
            try
            {
                return c.getDeclaredMethod(name, args);
            }
            catch (NoSuchMethodException e)
            {
                // No-op, continue the search
            }
        }
        return null;
    }

    private static String buildInvokeMethodErrorMessage(Method method, Object obj, Object... args)
    {
        StringBuilder message = new StringBuilder(String.format(
                "Exception invoking method [%s] on object [%s], using arguments [", method.getName(), obj));
        if (args != null)
        {
            for (int i = 0; i < args.length; i++)
            {
                message.append((i > 0 ? "," : "") + args[i]);
            }
        }
        message.append("]");
        return message.toString();
    }

    /**
     * Set the accessibility flag on the {@link AccessibleObject} as described in
     * {@link AccessibleObject#setAccessible(boolean)} within the context of a {@link PrivilegedAction}.
     *
     * @param <A>
     *            member the accessible object type
     * @param member
     *            the accessible object
     * @return the accessible object after the accessible flag has been altered
     */
    public static <A extends AccessibleObject> A setAccessible(final A member)
    {
        AccessController.doPrivileged(new PrivilegedAction<Void>()
        {
            @Override
            public Void run()
            {
                member.setAccessible(true);
                return null;
            }
        });
        return member;
    }

    /**
     * <p>
     * Invoke the specified method on the provided instance, passing any additional arguments included in this method as
     * arguments to the specified method.
     * </p>
     * <p/>
     * <p>
     * This method provides the same functionality and throws the same exceptions as
     * {@link Reflections#invokeMethod(boolean, Method, Class, Object, Object...)}, with the expected return type set to
     * {@link Object} and no change to the method's accessibility.
     * </p>
     *
     * @see Reflections#invokeMethod(boolean, Method, Class, Object, Object...)
     * @see Method#invoke(Object, Object...)
     */
    public static Object invokeMethod(Method method, Object instance, Object... args)
    {
        return invokeMethod(false, method, Object.class, instance, args);
    }

    /**
     * <p>
     * Invoke the specified method on the provided instance, passing any additional arguments included in this method as
     * arguments to the specified method.
     * </p>
     * <p/>
     * <p>
     * This method attempts to set the accessible flag of the method in a {@link PrivilegedAction} before invoking the
     * method if the first argument is true.
     * </p>
     * <p/>
     * <p>
     * This method provides the same functionality and throws the same exceptions as
     * {@link Reflections#invokeMethod(boolean, Method, Class, Object, Object...)}, with the expected return type set to
     * {@link Object}.
     * </p>
     *
     * @see Reflections#invokeMethod(boolean, Method, Class, Object, Object...)
     * @see Method#invoke(Object, Object...)
     */
    public static Object invokeMethod(boolean setAccessible, Method method, Object instance, Object... args)
    {
        return invokeMethod(setAccessible, method, Object.class, instance, args);
    }

    /**
     * <p>
     * Invoke the specified method on the provided instance, passing any additional arguments included in this method as
     * arguments to the specified method.
     * </p>
     * <p/>
     * <p>
     * This method provides the same functionality and throws the same exceptions as
     * {@link Reflections#invokeMethod(boolean, Method, Class, Object, Object...)}, with the expected return type set to
     * {@link Object} and honoring the accessibility of the method.
     * </p>
     *
     * @see Reflections#invokeMethod(boolean, Method, Class, Object, Object...)
     * @see Method#invoke(Object, Object...)
     */
    public static <T> T invokeMethod(Method method, Class<T> expectedReturnType, Object instance, Object... args)
    {
        return invokeMethod(false, method, expectedReturnType, instance, args);
    }

    /**
     * <p>
     * Invoke the method on the instance, with any arguments specified, casting the result of invoking the method to the
     * expected return type.
     * </p>
     * <p/>
     * <p>
     * This method wraps {@link Method#invoke(Object, Object...)}, converting the checked exceptions that
     * {@link Method#invoke(Object, Object...)} specifies to runtime exceptions.
     * </p>
     * <p/>
     * <p>
     * If instructed, this method attempts to set the accessible flag of the method in a {@link PrivilegedAction} before
     * invoking the method.
     * </p>
     *
     * @param setAccessible
     *            flag indicating whether method should first be set as accessible
     * @param method
     *            the method to invoke
     * @param instance
     *            the instance to invoke the method
     * @param args
     *            the arguments to the method
     * @return the result of invoking the method, or null if the method's return type is void
     * @throws RuntimeException
     *             if this <code>Method</code> object enforces Java language access control and the underlying method is
     *             inaccessible or if the underlying method throws an exception or if the initialization provoked by
     *             this method fails.
     * @throws IllegalArgumentException
     *             if the method is an instance method and the specified <code>instance</code> argument is not an
     *             instance of the class or interface declaring the underlying method (or of a subclass or implementor
     *             thereof); if the number of actual and formal parameters differ; if an unwrapping conversion for
     *             primitive arguments fails; or if, after possible unwrapping, a parameter value cannot be converted to
     *             the corresponding formal parameter type by a method invocation conversion.
     * @throws NullPointerException
     *             if the specified <code>instance</code> is null and the method is an instance method.
     * @throws ClassCastException
     *             if the result of invoking the method cannot be cast to the expectedReturnType
     * @throws ExceptionInInitializerError
     *             if the initialization provoked by this method fails.
     * @see Method#invoke(Object, Object...)
     */
    public static <T> T invokeMethod(boolean setAccessible, Method method, Class<T> expectedReturnType,
            Object instance, Object... args)
    {
        if (setAccessible && !method.isAccessible())
        {
            setAccessible(method);
        }

        try
        {
            return expectedReturnType.cast(method.invoke(instance, args));
        }
        catch (IllegalAccessException ex)
        {
            throw new RuntimeException(buildInvokeMethodErrorMessage(method, instance, args), ex);
        }
        catch (IllegalArgumentException ex)
        {
            throw new IllegalArgumentException(buildInvokeMethodErrorMessage(method, instance, args), ex);
        }
        catch (InvocationTargetException ex)
        {
            throw new RuntimeException(buildInvokeMethodErrorMessage(method, instance, args), ex.getCause());
        }
        catch (NullPointerException ex)
        {
            NullPointerException ex2 = new NullPointerException(buildInvokeMethodErrorMessage(method, instance, args));
            ex2.initCause(ex.getCause());
            throw ex2;
        }
        catch (ExceptionInInitializerError e)
        {
            ExceptionInInitializerError e2 = new ExceptionInInitializerError(buildInvokeMethodErrorMessage(method,
                    instance, args));
            e2.initCause(e.getCause());
            throw e2;
        }
    }

    /**
     * <p>
     * Set the value of a field on the instance to the specified value.
     * </p>
     * <p/>
     * <p>
     * This method provides the same functionality and throws the same exceptions as
     * {@link Reflections#setFieldValue(boolean, Method, Class, Object, Object...)}, honoring the accessibility of the
     * field.
     * </p>
     */
    public static void setFieldValue(Field field, Object instance, Object value)
    {
        setFieldValue(false, field, instance, value);
    }

    /**
     * <p>
     * Sets the value of a field on the instance to the specified value.
     * </p>
     * <p/>
     * <p>
     * This method wraps {@link Field#set(Object, Object)}, converting the checked exceptions that
     * {@link Field#set(Object, Object)} specifies to runtime exceptions.
     * </p>
     * <p/>
     * <p>
     * If instructed, this method attempts to set the accessible flag of the method in a {@link PrivilegedAction} before
     * invoking the method.
     * </p>
     *
     * @param field
     *            the field on which to operate, or null if the field is static
     * @param instance
     *            the instance on which the field value should be set upon
     * @param value
     *            the value to set the field to
     * @throws RuntimeException
     *             if the underlying field is inaccessible.
     * @throws IllegalArgumentException
     *             if the specified <code>instance</code> is not an instance of the class or interface declaring the
     *             underlying field (or a subclass or implementor thereof), or if an unwrapping conversion fails.
     * @throws NullPointerException
     *             if the specified <code>instance</code> is null and the field is an instance field.
     * @throws ExceptionInInitializerError
     *             if the initialization provoked by this method fails.
     * @see Field#set(Object, Object)
     */
    public static void setFieldValue(boolean setAccessible, Field field, Object instance, Object value)
    {
        if (setAccessible && !field.isAccessible())
        {
            setAccessible(field);
        }

        try
        {
            field.set(instance, value);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(buildSetFieldValueErrorMessage(field, instance, value), e);
        }
        catch (NullPointerException ex)
        {
            NullPointerException ex2 = new NullPointerException(buildSetFieldValueErrorMessage(field, instance, value));
            ex2.initCause(ex.getCause());
            throw ex2;
        }
        catch (ExceptionInInitializerError e)
        {
            ExceptionInInitializerError e2 = new ExceptionInInitializerError(buildSetFieldValueErrorMessage(field,
                    instance, value));
            e2.initCause(e.getCause());
            throw e2;
        }
    }

    private static String buildSetFieldValueErrorMessage(Field field, Object obj, Object value)
    {
        return String.format("Exception setting [%s] field on object [%s] to value [%s]", field.getName(), obj, value);
    }

    private static String buildGetFieldValueErrorMessage(Field field, Object obj)
    {
        return String.format("Exception reading [%s] field from object [%s].", field.getName(), obj);
    }

    public static Object getFieldValue(Field field, Object instance)
    {
        return getFieldValue(field, instance, Object.class);
    }

    /**
     * <p>
     * Get the value of the field, on the specified instance, casting the value of the field to the expected type.
     * </p>
     * <p/>
     * <p>
     * This method wraps {@link Field#get(Object)}, converting the checked exceptions that {@link Field#get(Object)}
     * specifies to runtime exceptions.
     * </p>
     *
     * @param <T>
     *            the type of the field's value
     * @param field
     *            the field to operate on
     * @param instance
     *            the instance from which to retrieve the value
     * @param expectedType
     *            the expected type of the field's value
     * @return the value of the field
     * @throws RuntimeException
     *             if the underlying field is inaccessible.
     * @throws IllegalArgumentException
     *             if the specified <code>instance</code> is not an instance of the class or interface declaring the
     *             underlying field (or a subclass or implementor thereof).
     * @throws NullPointerException
     *             if the specified <code>instance</code> is null and the field is an instance field.
     * @throws ExceptionInInitializerError
     *             if the initialization provoked by this method fails.
     */
    public static <T> T getFieldValue(Field field, Object instance, Class<T> expectedType)
    {
        try
        {
            return Reflections.cast(field.get(instance));
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(buildGetFieldValueErrorMessage(field, instance), e);
        }
        catch (NullPointerException ex)
        {
            NullPointerException ex2 = new NullPointerException(buildGetFieldValueErrorMessage(field, instance));
            ex2.initCause(ex.getCause());
            throw ex2;
        }
        catch (ExceptionInInitializerError e)
        {
            ExceptionInInitializerError e2 = new ExceptionInInitializerError(buildGetFieldValueErrorMessage(field,
                    instance));
            e2.initCause(e.getCause());
            throw e2;
        }
    }

    /**
     * Check if a class is serializable.
     *
     * @param clazz
     *            The class to check
     * @return true if the class implements serializable or is a primitive
     */
    public static boolean isSerializable(Class<?> clazz)
    {
        return clazz.isPrimitive() || Serializable.class.isAssignableFrom(clazz);
    }

    /**
     * Gets the property name from a getter method.
     * <p/>
     * We extend JavaBean conventions, allowing the getter method to have parameters
     *
     * @param method
     *            The getter method
     * @return The name of the property. Returns null if method wasn't JavaBean getter-styled
     */
    public static String getPropertyName(Method method)
    {
        String methodName = method.getName();
        if (methodName.matches("^(get).*"))
        {
            return Introspector.decapitalize(methodName.substring(3));
        }
        else if (methodName.matches("^(is).*"))
        {
            return Introspector.decapitalize(methodName.substring(2));
        }
        else
        {
            return null;
        }

    }

}
