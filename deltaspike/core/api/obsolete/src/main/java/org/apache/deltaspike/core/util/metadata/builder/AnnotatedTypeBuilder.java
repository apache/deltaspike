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

package org.apache.deltaspike.core.util.metadata.builder;

import org.apache.deltaspike.core.util.ReflectionUtils;
import org.apache.deltaspike.core.util.securitymanaged.SetAccessiblePrivilegedAction;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder to aid in creation of a new {@link AnnotatedType} for use in CDI life cycle events.
 * Using the builder is typically done by reading the annotations from a {@link Class} or an
 * {@link AnnotatedType}. Once the starting class or type has been added all of annotations
 * can be modified: constructor, parameter, class, method and fields.
 * <p/>
 * The AnnotatedTypeBuilder is not thread safe and shall not be used concurrently!
 */
public class AnnotatedTypeBuilder<X>
{
    private Class<X> javaClass;
    private final AnnotationBuilder typeAnnotations;

    private final Map<Constructor<?>, AnnotationBuilder> constructors;
    private final Map<Constructor<?>, Map<Integer, AnnotationBuilder>> constructorParameters;
    private final Map<Constructor<?>, Map<Integer, Type>> constructorParameterTypes;

    private final Map<Field, AnnotationBuilder> fields;
    private final Map<Field, Type> fieldTypes;

    private final Map<Method, AnnotationBuilder> methods;
    private final Map<Method, Map<Integer, AnnotationBuilder>> methodParameters;
    private final Map<Method, Map<Integer, Type>> methodParameterTypes;

    /**
     * Create a new builder. A new builder has no annotations and no members.
     *
     * @see #readFromType(AnnotatedType)
     * @see #readFromType(Class)
     * @see #readFromType(AnnotatedType, boolean)
     * @see #readFromType(Class, boolean)
     */
    public AnnotatedTypeBuilder()
    {
        typeAnnotations = new AnnotationBuilder();
        constructors = new HashMap<Constructor<?>, AnnotationBuilder>();
        constructorParameters = new HashMap<Constructor<?>, Map<Integer, AnnotationBuilder>>();
        constructorParameterTypes = new HashMap<Constructor<?>, Map<Integer, Type>>();
        fields = new HashMap<Field, AnnotationBuilder>();
        fieldTypes = new HashMap<Field, Type>();
        methods = new HashMap<Method, AnnotationBuilder>();
        methodParameters = new HashMap<Method, Map<Integer, AnnotationBuilder>>();
        methodParameterTypes = new HashMap<Method, Map<Integer, Type>>();
    }

    /**
     * Add an annotation to the type declaration.
     *
     * @param annotation the annotation instance to add
     * @throws IllegalArgumentException if the annotation is null
     */
    public AnnotatedTypeBuilder<X> addToClass(Annotation annotation)
    {
        typeAnnotations.add(annotation);
        return this;
    }

    /**
     * Remove an annotation from the type
     *
     * @param annotationType the annotation type to remove
     * @throws IllegalArgumentException if the annotationType
     */
    public AnnotatedTypeBuilder<X> removeFromClass(Class<? extends Annotation> annotationType)
    {
        typeAnnotations.remove(annotationType);
        return this;
    }

    /**
     * Add an annotation to the specified field. If the field is not already
     * present, it will be added.
     *
     * @param field      the field to add the annotation to
     * @param annotation the annotation to add
     * @throws IllegalArgumentException if the annotation is null
     */
    public AnnotatedTypeBuilder<X> addToField(Field field, Annotation annotation)
    {
        if (fields.get(field) == null)
        {
            fields.put(field, new AnnotationBuilder());
        }
        fields.get(field).add(annotation);
        return this;
    }

    /**
     * Add an annotation to the specified field. If the field is not already
     * present, it will be added.
     *
     * @param field      the field to add the annotation to
     * @param annotation the annotation to add
     * @throws IllegalArgumentException if the annotation is null
     */
    public AnnotatedTypeBuilder<X> addToField(AnnotatedField<? super X> field, Annotation annotation)
    {
        return addToField(field.getJavaMember(), annotation);
    }

    /**
     * Remove an annotation from the specified field.
     *
     * @param field          the field to remove the annotation from
     * @param annotationType the annotation type to remove
     * @throws IllegalArgumentException if the annotationType is null or if the
     *                                  field is not currently declared on the type
     */
    public AnnotatedTypeBuilder<X> removeFromField(Field field, Class<? extends Annotation> annotationType)
    {
        if (fields.get(field) == null)
        {
            throw new IllegalArgumentException("field " + field + " not present on class " + getJavaClass());
        }
        else
        {
            fields.get(field).remove(annotationType);
        }
        return this;
    }

    /**
     * Remove an annotation from the specified field.
     *
     * @param field          the field to remove the annotation from
     * @param annotationType the annotation type to remove
     * @throws IllegalArgumentException if the annotationType is null or if the
     *                                  field is not currently declared on the type
     */
    public AnnotatedTypeBuilder<X> removeFromField(AnnotatedField<? super X> field,
                                                   Class<? extends Annotation> annotationType)
    {
        return removeFromField(field.getJavaMember(), annotationType);
    }

    /**
     * Add an annotation to the specified method. If the method is not already
     * present, it will be added.
     *
     * @param method     the method to add the annotation to
     * @param annotation the annotation to add
     * @throws IllegalArgumentException if the annotation is null
     */
    public AnnotatedTypeBuilder<X> addToMethod(Method method, Annotation annotation)
    {
        if (methods.get(method) == null)
        {
            methods.put(method, new AnnotationBuilder());
        }
        methods.get(method).add(annotation);
        return this;
    }

    /**
     * Add an annotation to the specified method. If the method is not already
     * present, it will be added.
     *
     * @param method     the method to add the annotation to
     * @param annotation the annotation to add
     * @throws IllegalArgumentException if the annotation is null
     */
    public AnnotatedTypeBuilder<X> addToMethod(AnnotatedMethod<? super X> method, Annotation annotation)
    {
        return addToMethod(method.getJavaMember(), annotation);
    }

    /**
     * Remove an annotation from the specified method.
     *
     * @param method         the method to remove the annotation from
     * @param annotationType the annotation type to remove
     * @throws IllegalArgumentException if the annotationType is null or if the
     *                                  method is not currently declared on the type
     */
    public AnnotatedTypeBuilder<X> removeFromMethod(Method method, Class<? extends Annotation> annotationType)
    {
        if (methods.get(method) == null)
        {
            throw new IllegalArgumentException("Method " + method + " not present on class" + getJavaClass());
        }
        else
        {
            methods.get(method).remove(annotationType);
        }
        return this;
    }

    /**
     * Remove an annotation from the specified method.
     *
     * @param method         the method to remove the annotation from
     * @param annotationType the annotation type to remove
     * @throws IllegalArgumentException if the annotationType is null or if the
     *                                  method is not currently declared on the type
     */
    public AnnotatedTypeBuilder<X> removeFromMethod(AnnotatedMethod<? super X> method,
                                                    Class<? extends Annotation> annotationType)
    {
        return removeFromMethod(method.getJavaMember(), annotationType);
    }

    /**
     * Add an annotation to the specified method parameter. If the method is not
     * already present, it will be added. If the method parameter is not already
     * present, it will be added.
     *
     * @param method     the method to add the annotation to
     * @param position   the position of the parameter to add
     * @param annotation the annotation to add
     * @throws IllegalArgumentException if the annotation is null
     */
    public AnnotatedTypeBuilder<X> addToMethodParameter(Method method, int position, Annotation annotation)
    {
        if (!methods.containsKey(method))
        {
            methods.put(method, new AnnotationBuilder());
        }
        if (methodParameters.get(method) == null)
        {
            methodParameters.put(method, new HashMap<Integer, AnnotationBuilder>());
        }
        if (methodParameters.get(method).get(position) == null)
        {
            methodParameters.get(method).put(position, new AnnotationBuilder());
        }
        methodParameters.get(method).get(position).add(annotation);
        return this;
    }

    /**
     * Remove an annotation from the specified method parameter.
     *
     * @param method         the method to remove the annotation from
     * @param position       the position of the parameter to remove
     * @param annotationType the annotation type to remove
     * @throws IllegalArgumentException if the annotationType is null, if the
     *                                  method is not currently declared on the type or if the
     *                                  parameter is not declared on the method
     */
    public AnnotatedTypeBuilder<X> removeFromMethodParameter(Method method,
                                                             int position, Class<? extends Annotation> annotationType)
    {
        if (methods.get(method) == null)
        {
            throw new IllegalArgumentException("Method " + method + " not present on class " + getJavaClass());
        }
        else
        {
            if (methodParameters.get(method).get(position) == null)
            {
                throw new IllegalArgumentException(
                        String.format("parameter %s not present on method %s declared on class %s",
                                method, position, getJavaClass()));
            }
            else
            {
                methodParameters.get(method).get(position).remove(annotationType);
            }
        }
        return this;
    }

    /**
     * Add an annotation to the specified constructor. If the constructor is not
     * already present, it will be added.
     *
     * @param constructor the constructor to add the annotation to
     * @param annotation  the annotation to add
     * @throws IllegalArgumentException if the annotation is null
     */
    public AnnotatedTypeBuilder<X> addToConstructor(Constructor<X> constructor, Annotation annotation)
    {
        if (constructors.get(constructor) == null)
        {
            constructors.put(constructor, new AnnotationBuilder());
        }
        constructors.get(constructor).add(annotation);
        return this;
    }

    /**
     * Add an annotation to the specified constructor. If the constructor is not
     * already present, it will be added.
     *
     * @param constructor the constructor to add the annotation to
     * @param annotation  the annotation to add
     * @throws IllegalArgumentException if the annotation is null
     */
    public AnnotatedTypeBuilder<X> addToConstructor(AnnotatedConstructor<X> constructor, Annotation annotation)
    {
        return addToConstructor(constructor.getJavaMember(), annotation);
    }

    /**
     * Remove an annotation from the specified constructor.
     *
     * @param constructor    the constructor to add the annotation to
     * @param annotationType the annotation to add
     * @throws IllegalArgumentException if the annotationType is null or if the
     *                                  constructor is not currently declared on the type
     */
    public AnnotatedTypeBuilder<X> removeFromConstructor(Constructor<X> constructor,
                                                         Class<? extends Annotation> annotationType)
    {
        if (constructors.get(constructor) != null)
        {
            constructors.get(constructor).remove(annotationType);
        }
        return this;
    }

    /**
     * Remove an annotation from the specified constructor.
     *
     * @param constructor    the constructor to add the annotation to
     * @param annotationType the annotation to add
     * @throws IllegalArgumentException if the annotationType is null, if the
     *                                  annotation does not exist on the type or if the constructor is
     *                                  not currently declared on the type
     */
    public AnnotatedTypeBuilder<X> removeFromConstructor(AnnotatedConstructor<X> constructor,
                                                         Class<? extends Annotation> annotationType)
    {
        return removeFromConstructor(constructor.getJavaMember(), annotationType);
    }

    /**
     * Add an annotation to the specified constructor parameter. If the
     * constructor is not already present, it will be added. If the constructor
     * parameter is not already present, it will be added.
     *
     * @param constructor the constructor to add the annotation to
     * @param position    the position of the parameter to add
     * @param annotation  the annotation to add
     * @throws IllegalArgumentException if the annotation is null
     */
    public AnnotatedTypeBuilder<X> addToConstructorParameter(Constructor<X> constructor,
                                                             int position,
                                                             Annotation annotation)
    {
        if (!constructors.containsKey(constructor))
        {
            constructors.put(constructor, new AnnotationBuilder());
        }
        if (constructorParameters.get(constructor) == null)
        {
            constructorParameters.put(constructor, new HashMap<Integer, AnnotationBuilder>());
        }
        if (constructorParameters.get(constructor).get(position) == null)
        {
            constructorParameters.get(constructor).put(position, new AnnotationBuilder());
        }
        constructorParameters.get(constructor).get(position).add(annotation);
        return this;
    }

    /**
     * Remove an annotation from the specified constructor parameter.
     *
     * @param constructor    the constructor to remove the annotation from
     * @param position       the position of the parameter to remove
     * @param annotationType the annotation type to remove
     * @throws IllegalArgumentException if the annotationType is null, if the
     *                                  constructor is not currently declared on the type or if the
     *                                  parameter is not declared on the constructor
     */
    public AnnotatedTypeBuilder<X> removeFromConstructorParameter(Constructor<X> constructor,
                                                                  int position,
                                                                  Class<? extends Annotation> annotationType)
    {
        if (constructorParameters.get(constructor) != null &&
                constructorParameters.get(constructor).get(position) != null)
        {
            constructorParameters.get(constructor).get(position).remove(annotationType);
        }
        return this;
    }

    /**
     * Remove an annotation from the specified parameter.
     *
     * @param parameter      the parameter to remove the annotation from
     * @param annotationType the annotation type to remove
     * @throws IllegalArgumentException if the annotationType is null, if the
     *                                  callable which declares the parameter is not currently declared
     *                                  on the type or if the parameter is not declared on either a
     *                                  constructor or a method
     */
    public AnnotatedTypeBuilder<X> removeFromParameter(AnnotatedParameter<? super X> parameter,
                                                       Class<? extends Annotation> annotationType)
    {
        if (parameter.getDeclaringCallable().getJavaMember() instanceof Method)
        {
            Method method = (Method) parameter.getDeclaringCallable().getJavaMember();
            return removeFromMethodParameter(method, parameter.getPosition(), annotationType);
        }
        if (parameter.getDeclaringCallable().getJavaMember() instanceof Constructor<?>)
        {
            @SuppressWarnings("unchecked")
            Constructor<X> constructor = (Constructor<X>) parameter.getDeclaringCallable().getJavaMember();
            return removeFromConstructorParameter(constructor, parameter.getPosition(), annotationType);
        }
        else
        {
            throw new IllegalArgumentException("Cannot remove from parameter " + parameter +
                    " - cannot operate on member " + parameter.getDeclaringCallable().getJavaMember());
        }
    }

    /**
     * Add an annotation to the specified parameter. If the callable which
     * declares the parameter is not already present, it will be added. If the
     * parameter is not already present on the callable, it will be added.
     *
     * @param parameter  the parameter to add the annotation to
     * @param annotation the annotation to add
     * @throws IllegalArgumentException if the annotation is null or if the
     *                                  parameter is not declared on either a constructor or a method
     */
    public AnnotatedTypeBuilder<X> addToParameter(AnnotatedParameter<? super X> parameter, Annotation annotation)
    {
        if (parameter.getDeclaringCallable().getJavaMember() instanceof Method)
        {
            Method method = (Method) parameter.getDeclaringCallable().getJavaMember();
            return addToMethodParameter(method, parameter.getPosition(), annotation);
        }
        if (parameter.getDeclaringCallable().getJavaMember() instanceof Constructor<?>)
        {
            @SuppressWarnings("unchecked")
            Constructor<X> constructor = (Constructor<X>) parameter.getDeclaringCallable().getJavaMember();
            return addToConstructorParameter(constructor, parameter.getPosition(), annotation);
        }
        else
        {
            throw new IllegalArgumentException("Cannot remove from parameter " + parameter +
                    " - cannot operate on member " + parameter.getDeclaringCallable().getJavaMember());
        }
    }

    /**
     * Remove annotations from the type, and all of it's members. If an
     * annotation of the specified type appears on the type declaration, or any
     * of it's members it will be removed.
     *
     * @param annotationType the type of annotation to remove
     * @throws IllegalArgumentException if the annotationType is null
     */
    public AnnotatedTypeBuilder<X> removeFromAll(Class<? extends Annotation> annotationType)
    {
        if (annotationType == null)
        {
            throw new IllegalArgumentException(String.format("%s parameter must not be null", "annotationType"));
        }
        removeFromClass(annotationType);
        for (Map.Entry<Field, AnnotationBuilder> field : fields.entrySet())
        {
            field.getValue().remove(annotationType);
        }
        for (Map.Entry<Method, AnnotationBuilder> method : methods.entrySet())
        {
            method.getValue().remove(annotationType);
            if (methodParameters.get(method.getKey()) != null)
            {
                for (Map.Entry<Integer, AnnotationBuilder> parameter : methodParameters.get(method.getKey()).entrySet())
                {
                    parameter.getValue().remove(annotationType);
                }
            }
        }
        for (Map.Entry<Constructor<?>, AnnotationBuilder> constructor : constructors.entrySet())
        {
            constructor.getValue().remove(annotationType);
            if (constructorParameters.get(constructor.getKey()) != null)
            {
                for (Map.Entry<Integer, AnnotationBuilder> parameter :
                        constructorParameters.get(constructor.getKey()).entrySet())
                {
                    parameter.getValue().remove(annotationType);
                }
            }
        }
        return this;
    }

    /**
     * Reads in from an existing AnnotatedType. Any elements not present are
     * added. The javaClass will be read in. If the annotation already exists on
     * that element in the builder the read annotation will be used.
     *
     * @param type the type to read from
     * @throws IllegalArgumentException if type is null
     */
    public AnnotatedTypeBuilder<X> readFromType(AnnotatedType<X> type)
    {
        return readFromType(type, true);
    }

    /**
     * Reads in from an existing AnnotatedType. Any elements not present are
     * added. The javaClass will be read in if overwrite is true. If the
     * annotation already exists on that element in the builder, overwrite
     * determines whether the original or read annotation will be used.
     *
     * @param type      the type to read from
     * @param overwrite if true, the read annotation will replace any existing
     *                  annotation
     * @throws IllegalArgumentException if type is null
     */
    public AnnotatedTypeBuilder<X> readFromType(AnnotatedType<X> type, boolean overwrite)
    {
        if (type == null)
        {
            throw new IllegalArgumentException(String.format("%s parameter must not be null", "type"));
        }
        if (javaClass == null || overwrite)
        {
            javaClass = type.getJavaClass();
        }
        mergeAnnotationsOnElement(type, overwrite, typeAnnotations);
        for (AnnotatedField<? super X> field : type.getFields())
        {
            if (fields.get(field.getJavaMember()) == null)
            {
                fields.put(field.getJavaMember(), new AnnotationBuilder());
            }
            mergeAnnotationsOnElement(field, overwrite, fields.get(field.getJavaMember()));
        }
        for (AnnotatedMethod<? super X> method : type.getMethods())
        {
            if (methods.get(method.getJavaMember()) == null)
            {
                methods.put(method.getJavaMember(), new AnnotationBuilder());
            }
            mergeAnnotationsOnElement(method, overwrite, methods.get(method.getJavaMember()));
            for (AnnotatedParameter<? super X> p : method.getParameters())
            {
                if (methodParameters.get(method.getJavaMember()) == null)
                {
                    methodParameters.put(method.getJavaMember(), new HashMap<Integer, AnnotationBuilder>());
                }
                if (methodParameters.get(method.getJavaMember()).get(p.getPosition()) == null)
                {
                    methodParameters.get(method.getJavaMember()).put(p.getPosition(), new AnnotationBuilder());
                }
                mergeAnnotationsOnElement(
                        p, overwrite, methodParameters.get(method.getJavaMember()).get(p.getPosition()));
            }
        }
        for (AnnotatedConstructor<? super X> constructor : type.getConstructors())
        {
            if (constructors.get(constructor.getJavaMember()) == null)
            {
                constructors.put(constructor.getJavaMember(), new AnnotationBuilder());
            }
            mergeAnnotationsOnElement(constructor, overwrite, constructors.get(constructor.getJavaMember()));
            for (AnnotatedParameter<? super X> p : constructor.getParameters())
            {
                if (constructorParameters.get(
                        constructor.getJavaMember()) == null)
                {
                    constructorParameters.put(
                            constructor.getJavaMember(), new HashMap<Integer, AnnotationBuilder>());
                }
                if (constructorParameters.get(
                        constructor.getJavaMember()).get(p.getPosition()) == null)
                {
                    constructorParameters.get(
                            constructor.getJavaMember()).put(p.getPosition(), new AnnotationBuilder());
                }
                mergeAnnotationsOnElement(
                        p, overwrite, constructorParameters.get(constructor.getJavaMember()).get(p.getPosition()));
            }
        }
        return this;
    }

    /**
     * Reads the annotations from an existing java type. Annotations already
     * present will be overwritten
     *
     * @param type the type to read from
     * @throws IllegalArgumentException if type is null
     */
    public AnnotatedTypeBuilder<X> readFromType(Class<X> type)
    {
        return readFromType(type, true);
    }

    /**
     * Reads the annotations from an existing java type. If overwrite is true
     * then existing annotations will be overwritten
     *
     * @param type      the type to read from
     * @param overwrite if true, the read annotation will replace any existing
     *                  annotation
     */
    public AnnotatedTypeBuilder<X> readFromType(Class<X> type, boolean overwrite)
    {
        if (type == null)
        {
            throw new IllegalArgumentException(String.format("%s parameter must not be null", "type"));
        }
        if (javaClass == null || overwrite)
        {
            javaClass = type;
        }
        for (Annotation annotation : type.getAnnotations())
        {
            if (overwrite || !typeAnnotations.isAnnotationPresent(annotation.annotationType()))
            {
                typeAnnotations.add(annotation);
            }
        }

        for (Field field : ReflectionUtils.getAllDeclaredFields(type))
        {
            AnnotationBuilder annotationBuilder = fields.get(field);
            if (annotationBuilder == null)
            {
                annotationBuilder = new AnnotationBuilder();
                fields.put(field, annotationBuilder);
            }

            if (System.getSecurityManager() != null)
            {
                AccessController.doPrivileged(new SetAccessiblePrivilegedAction(field));
            }
            else
            {
                field.setAccessible(true);
            }

            for (Annotation annotation : field.getAnnotations())
            {
                if (overwrite || !annotationBuilder.isAnnotationPresent(annotation.annotationType()))
                {
                    annotationBuilder.add(annotation);
                }
            }
        }

        for (Method method : ReflectionUtils.getAllDeclaredMethods(type))
        {
            AnnotationBuilder annotationBuilder = methods.get(method);
            if (annotationBuilder == null)
            {
                annotationBuilder = new AnnotationBuilder();
                methods.put(method, annotationBuilder);
            }

            if (System.getSecurityManager() != null)
            {
                AccessController.doPrivileged(new SetAccessiblePrivilegedAction(method));
            }
            else
            {
                method.setAccessible(true);
            }

            for (Annotation annotation : method.getAnnotations())
            {
                if (overwrite || !annotationBuilder.isAnnotationPresent(annotation.annotationType()))
                {
                    annotationBuilder.add(annotation);
                }
            }

            Map<Integer, AnnotationBuilder> parameters = methodParameters.get(method);
            if (parameters == null)
            {
                parameters = new HashMap<Integer, AnnotationBuilder>();
                methodParameters.put(method, parameters);
            }
            for (int i = 0; i < method.getParameterTypes().length; ++i)
            {
                AnnotationBuilder parameterAnnotationBuilder = parameters.get(i);
                if (parameterAnnotationBuilder == null)
                {
                    parameterAnnotationBuilder = new AnnotationBuilder();
                    parameters.put(i, parameterAnnotationBuilder);
                }
                for (Annotation annotation : method.getParameterAnnotations()[i])
                {
                    if (overwrite || !parameterAnnotationBuilder.isAnnotationPresent(annotation.annotationType()))
                    {
                        parameterAnnotationBuilder.add(annotation);
                    }
                }
            }
        }

        for (Constructor<?> constructor : type.getDeclaredConstructors())
        {
            AnnotationBuilder annotationBuilder = constructors.get(constructor);
            if (annotationBuilder == null)
            {
                annotationBuilder = new AnnotationBuilder();
                constructors.put(constructor, annotationBuilder);
            }
            constructor.setAccessible(true);
            for (Annotation annotation : constructor.getAnnotations())
            {
                if (overwrite || !annotationBuilder.isAnnotationPresent(annotation.annotationType()))
                {
                    annotationBuilder.add(annotation);
                }
            }
            Map<Integer, AnnotationBuilder> mparams = constructorParameters.get(constructor);
            if (mparams == null)
            {
                mparams = new HashMap<Integer, AnnotationBuilder>();
                constructorParameters.put(constructor, mparams);
            }
            for (int i = 0; i < constructor.getParameterTypes().length; ++i)
            {
                AnnotationBuilder parameterAnnotationBuilder = mparams.get(i);
                if (parameterAnnotationBuilder == null)
                {
                    parameterAnnotationBuilder = new AnnotationBuilder();
                    mparams.put(i, parameterAnnotationBuilder);
                }
                for (Annotation annotation : constructor.getParameterAnnotations()[i])
                {
                    if (overwrite || !parameterAnnotationBuilder.isAnnotationPresent(annotation.annotationType()))
                    {
                        annotationBuilder.add(annotation);
                    }
                }
            }
        }
        return this;
    }

    protected void mergeAnnotationsOnElement(Annotated annotated,
                                             boolean overwriteExisting,
                                             AnnotationBuilder typeAnnotations)
    {
        for (Annotation annotation : annotated.getAnnotations())
        {
            if (typeAnnotations.getAnnotation(annotation.annotationType()) != null)
            {
                if (overwriteExisting)
                {
                    typeAnnotations.remove(annotation.annotationType());
                    typeAnnotations.add(annotation);
                }
            }
            else
            {
                typeAnnotations.add(annotation);
            }
        }
    }

    /**
     * Create an {@link AnnotatedType}. Any public members present on the
     * underlying class and not overridden by the builder will be automatically
     * added.
     */
    public AnnotatedType<X> create()
    {
        Map<Constructor<?>, Map<Integer, AnnotationStore>> constructorParameterAnnotations =
                new HashMap<Constructor<?>, Map<Integer, AnnotationStore>>();
        Map<Constructor<?>, AnnotationStore> constructorAnnotations =
                new HashMap<Constructor<?>, AnnotationStore>();
        Map<Method, Map<Integer, AnnotationStore>> methodParameterAnnotations =
                new HashMap<Method, Map<Integer, AnnotationStore>>();
        Map<Method, AnnotationStore> methodAnnotations =
                new HashMap<Method, AnnotationStore>();
        Map<Field, AnnotationStore> fieldAnnotations =
                new HashMap<Field, AnnotationStore>();

        for (Map.Entry<Field, AnnotationBuilder> field : fields.entrySet())
        {
            fieldAnnotations.put(field.getKey(), field.getValue().create());
        }

        for (Map.Entry<Method, AnnotationBuilder> method : methods.entrySet())
        {
            methodAnnotations.put(method.getKey(), method.getValue().create());
        }
        for (Map.Entry<Method, Map<Integer, AnnotationBuilder>> parameters : methodParameters.entrySet())
        {
            Map<Integer, AnnotationStore> parameterAnnotations = new HashMap<Integer, AnnotationStore>();
            methodParameterAnnotations.put(parameters.getKey(), parameterAnnotations);
            for (Map.Entry<Integer, AnnotationBuilder> parameter : parameters.getValue().entrySet())
            {
                parameterAnnotations.put(parameter.getKey(), parameter.getValue().create());
            }
        }

        for (Map.Entry<Constructor<?>, AnnotationBuilder> constructor : constructors.entrySet())
        {
            constructorAnnotations.put(constructor.getKey(), constructor.getValue().create());
        }
        for (Map.Entry<Constructor<?>, Map<Integer, AnnotationBuilder>> parameters : constructorParameters.entrySet())
        {
            Map<Integer, AnnotationStore> parameterAnnotations = new HashMap<Integer, AnnotationStore>();
            constructorParameterAnnotations.put(parameters.getKey(), parameterAnnotations);
            for (Map.Entry<Integer, AnnotationBuilder> parameter : parameters.getValue().entrySet())
            {
                parameterAnnotations.put(parameter.getKey(), parameter.getValue().create());
            }
        }

        return new AnnotatedTypeImpl<X>(javaClass, typeAnnotations.create(), fieldAnnotations, methodAnnotations,
                methodParameterAnnotations, constructorAnnotations, constructorParameterAnnotations, fieldTypes,
                methodParameterTypes, constructorParameterTypes);
    }

    /**
     * Override the declared type of a field
     *
     * @param field the field to override the type on
     * @param type  the new type of the field
     * @throws IllegalArgumentException if field or type is null
     */
    public void overrideFieldType(Field field, Type type)
    {
        if (field == null)
        {
            throw new IllegalArgumentException(String.format("%s parameter must not be null", "field"));
        }
        if (type == null)
        {
            throw new IllegalArgumentException(String.format("%s parameter must not be null", "type"));
        }
        fieldTypes.put(field, type);
    }

    /**
     * Override the declared type of a field
     *
     * @param field the field to override the type on
     * @param type  the new type of the field
     * @throws IllegalArgumentException if field or type is null
     */
    public void overrideFieldType(AnnotatedField<? super X> field, Type type)
    {
        overrideFieldType(field.getJavaMember(), type);
    }

    /**
     * Override the declared type of a method parameter
     *
     * @param method   the method to override the parameter type on
     * @param position the position of the parameter to override the type on
     * @param type     the new type of the parameter
     * @throws IllegalArgumentException if parameter or type is null
     */
    public AnnotatedTypeBuilder<X> overrideMethodParameterType(Method method, int position, Type type)
    {
        if (method == null)
        {
            throw new IllegalArgumentException(String.format("%s parameter must not be null", "method"));
        }
        if (type == null)
        {
            throw new IllegalArgumentException(String.format("%s parameter must not be null", "type"));
        }
        if (methodParameterTypes.get(method) == null)
        {
            methodParameterTypes.put(method, new HashMap<Integer, Type>());
        }
        methodParameterTypes.get(method).put(position, type);
        return this;
    }

    /**
     * Override the declared type of a constructor parameter
     *
     * @param constructor the constructor to override the parameter type on
     * @param position    the position of the parameter to override the type on
     * @param type        the new type of the parameter
     * @throws IllegalArgumentException if parameter or type is null
     */
    public AnnotatedTypeBuilder<X> overrideConstructorParameterType(Constructor<X> constructor, int position, Type type)
    {
        if (constructor == null)
        {
            throw new IllegalArgumentException(String.format("%s parameter must not be null", "constructor"));
        }
        if (type == null)
        {
            throw new IllegalArgumentException(String.format("%s parameter must not be null", "type"));
        }
        if (constructorParameterTypes.get(constructor) == null)
        {
            constructorParameterTypes.put(constructor, new HashMap<Integer, Type>());
        }
        constructorParameterTypes.get(constructor).put(position, type);
        return this;
    }

    /**
     * Override the declared type of a parameter.
     *
     * @param parameter the parameter to override the type on
     * @param type      the new type of the parameter
     * @throws IllegalArgumentException if parameter or type is null
     */
    public AnnotatedTypeBuilder<X> overrideParameterType(AnnotatedParameter<? super X> parameter, Type type)
    {
        if (parameter.getDeclaringCallable().getJavaMember() instanceof Method)
        {
            Method method = (Method) parameter.getDeclaringCallable().getJavaMember();
            return overrideMethodParameterType(method, parameter.getPosition(), type);
        }
        if (parameter.getDeclaringCallable().getJavaMember() instanceof Constructor<?>)
        {
            @SuppressWarnings("unchecked")
            Constructor<X> constructor = (Constructor<X>) parameter.getDeclaringCallable().getJavaMember();
            return overrideConstructorParameterType(constructor, parameter.getPosition(), type);
        }
        else
        {
            throw new IllegalArgumentException("Cannot remove from parameter " + parameter +
                    " - cannot operate on member " + parameter.getDeclaringCallable().getJavaMember());
        }
    }

    /**
     * getter for the class
     */
    public Class<X> getJavaClass()
    {
        return javaClass;
    }

    /**
     * setter for the class
     */
    public AnnotatedTypeBuilder<X> setJavaClass(Class<X> javaClass)
    {
        this.javaClass = javaClass;
        return this;
    }
}
