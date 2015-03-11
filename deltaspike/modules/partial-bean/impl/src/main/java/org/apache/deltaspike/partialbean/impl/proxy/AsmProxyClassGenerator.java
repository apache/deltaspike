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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import javax.enterprise.inject.Typed;
import org.apache.deltaspike.partialbean.impl.interception.ManualInvocationHandler;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

@Typed
public abstract class AsmProxyClassGenerator
{
    private static final String FIELDNAME_HANDLER = "__handler";

    private static final Type TYPE_CLASS = Type.getType(Class.class);
    private static final Type TYPE_OBJECT = Type.getType(Object.class);

    private AsmProxyClassGenerator()
    {
        // prevent instantiation
    }

    public static <T> Class<T> generateProxyClass(ClassLoader classLoader,
            Class<T> targetClass,
            Class<? extends InvocationHandler> invocationHandlerClass,
            String suffix,
            java.lang.reflect.Method[] redirectMethods,
            java.lang.reflect.Method[] interceptionMethods)
    {
        String proxyName = targetClass.getCanonicalName() + suffix;
        String classFileName = proxyName.replace('.', '/');

        byte[] proxyBytes = generateProxyClassBytes(targetClass, invocationHandlerClass,
                classFileName, redirectMethods, interceptionMethods);

        Class<T> proxyClass = (Class<T>) loadClass(classLoader, proxyName, proxyBytes);

        return proxyClass;
    }

    private static byte[] generateProxyClassBytes(Class<?> targetClass,
            Class<? extends InvocationHandler> invocationHandlerClass,
            String proxyName,
            java.lang.reflect.Method[] redirectMethods,
            java.lang.reflect.Method[] interceptionMethods)
    {
        Class<?> superClass = targetClass;
        String[] interfaces = new String[] { };

        if (targetClass.isInterface())
        {
            superClass = Object.class;
            interfaces = new String[] { Type.getInternalName(targetClass) };
        }

        // add PartialBeanProxy as interface
        interfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
        interfaces[interfaces.length - 1] = Type.getInternalName(PartialBeanProxy.class);

        Type superType = Type.getType(superClass);
        Type proxyType = Type.getObjectType(proxyName);
        Type invocationHandlerType = Type.getType(invocationHandlerClass);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, proxyType.getInternalName(), null,
                superType.getInternalName(), interfaces);

        // copy annotations
        for (Annotation annotation : targetClass.getDeclaredAnnotations())
        {
            cw.visitAnnotation(Type.getDescriptor(annotation.annotationType()), true).visitEnd();
        }

        defineInvocationHandlerField(cw, invocationHandlerType);
        defineConstructor(cw, proxyType, superType);
        definePartialBeanProxyMethods(cw, proxyType, invocationHandlerType);

        for (java.lang.reflect.Method method : redirectMethods)
        {
            defineMethod(cw, method, proxyType, invocationHandlerType, superType, true);
        }

        for (java.lang.reflect.Method method : interceptionMethods)
        {
            defineMethod(cw, method, proxyType, invocationHandlerType, superType, false);
        }

        return cw.toByteArray();
    }

    private static void defineInvocationHandlerField(ClassWriter cw, Type invocationHandlerType)
    {
        // generates
        // private MyPartialBeanInvocationHandler __handler;
        cw.visitField(Opcodes.ACC_PRIVATE, FIELDNAME_HANDLER, invocationHandlerType.getDescriptor(), null, null)
                .visitEnd();
    }

    private static void defineConstructor(ClassWriter cw, Type proxyType, Type superType)
    {
        GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC,
                new Method("<init>", Type.VOID_TYPE, new Type[]{ }),
                null,
                null,
                cw);

        mg.visitCode();

        // invoke super constructor
        mg.loadThis();
        mg.invokeConstructor(superType, Method.getMethod("void <init> ()"));
        mg.returnValue();
        mg.endMethod();

        mg.visitEnd();
    }

    private static void definePartialBeanProxyMethods(ClassWriter cw, Type proxyType, Type invocationHandlerType)
    {
        try
        {
            // implement #setRedirectInvocationHandler
            Method asmMethod = Method.getMethod(
                            PartialBeanProxy.class.getDeclaredMethod("setRedirectInvocationHandler",
                            InvocationHandler.class));
            GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, asmMethod, null, null, cw);

            mg.visitCode();

            mg.loadThis();
            mg.loadArg(0);
            mg.checkCast(invocationHandlerType);
            mg.putField(proxyType, FIELDNAME_HANDLER, invocationHandlerType);
            mg.returnValue();

            mg.visitMaxs(2, 1);
            mg.visitEnd();


            // implement #getRedirectInvocationHandler
            asmMethod = Method.getMethod(PartialBeanProxy.class.getDeclaredMethod("getRedirectInvocationHandler"));
            mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, asmMethod, null, null, cw);

            mg.visitCode();

            mg.loadThis();
            mg.getField(proxyType, FIELDNAME_HANDLER, invocationHandlerType);
            mg.returnValue();

            mg.visitMaxs(2, 1);
            mg.visitEnd();
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalStateException("Unable to implement " + PartialBeanProxy.class.getName(), e);
        }
    }

    private static void defineMethod(ClassWriter cw, java.lang.reflect.Method method, Type proxyType,
            Type invocationHandlerType, Type superType, boolean callInvocationHandler)
    {
        Type methodType = Type.getType(method);
        Type[] exceptionTypes = getTypes(method.getExceptionTypes());

        // push the method definition
        int modifiers = (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED) & method.getModifiers();
        Method asmMethod = Method.getMethod(method);
        GeneratorAdapter mg = new GeneratorAdapter(modifiers, asmMethod, null, exceptionTypes, cw);

        // copy annotations
        for (Annotation annotation : method.getDeclaredAnnotations())
        {
            mg.visitAnnotation(Type.getDescriptor(annotation.annotationType()), true).visitEnd();
        }

        mg.visitCode();

        Label tryBlockStart = mg.mark();

        mg.loadThis();
        loadCurrentMethod(mg, method, methodType);
        loadArguments(mg, method, methodType);

        // invoke our ProxyInvocationHandler and store it in a local variable
        int manualInvocationHandlerReturnValue = mg.newLocal(TYPE_OBJECT);
        mg.invokeStatic(Type.getType(ManualInvocationHandler.class),
                Method.getMethod("Object staticInvoke(Object, java.lang.reflect.Method, Object[])"));
        mg.storeLocal(manualInvocationHandlerReturnValue);
        
        // check if ManualInvocationHandler returned the PROCEED_ORIGINAL object
        // if true, we switch to our special logic, otherwise return the returned value
        Label proceedOriginalStart = new Label();
        mg.getStatic(Type.getType(ManualInvocationHandler.class), "PROCEED_ORIGINAL", TYPE_OBJECT);
        mg.loadLocal(manualInvocationHandlerReturnValue);
        mg.ifCmp(TYPE_OBJECT, GeneratorAdapter.EQ, proceedOriginalStart);
        
        // cast the result
        mg.loadLocal(manualInvocationHandlerReturnValue);
        mg.unbox(methodType.getReturnType());

        Label tryBlockEnd = mg.mark();

        // push return
        mg.returnValue();

        mg.mark(proceedOriginalStart);
        if (callInvocationHandler)
        {
            // call stored InvocationHandler
            mg.loadThis();
            mg.getField(proxyType, FIELDNAME_HANDLER, invocationHandlerType);
            mg.loadThis();
            loadCurrentMethod(mg, method, methodType);
            loadArguments(mg, method, methodType);
            mg.invokeVirtual(invocationHandlerType,
                    Method.getMethod("Object invoke(Object, java.lang.reflect.Method, Object[])"));
            mg.unbox(methodType.getReturnType());
            mg.returnValue();
        }
        else
        {
            // call super method
            mg.loadThis();
            mg.loadArgs();
            mg.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    superType.getInternalName(),
                    method.getName(),
                    Type.getMethodDescriptor(method),
                    false);
            mg.returnValue();
        }
        
        
        
        boolean throwableCatched = false;

        // catch declared exceptions
        if (exceptionTypes.length > 0)
        {
            Label rethrow = mg.mark();
            mg.visitVarInsn(Opcodes.ASTORE, 1);
            mg.visitVarInsn(Opcodes.ALOAD, 1);
            mg.throwException();

            // catch declared exceptions and rethrow it...
            for (Type exceptionType : exceptionTypes)
            {
                if (exceptionType.getClassName().equals(Throwable.class.getName()))
                {
                    throwableCatched = true;
                }
                mg.visitTryCatchBlock(tryBlockStart, tryBlockEnd, rethrow, exceptionType.getInternalName());
            }
        }

        if (!throwableCatched)
        {
            // catch Throwable and wrap it with a UndeclaredThrowableException
            Type uteType = Type.getType(UndeclaredThrowableException.class);
            Label wrapAndRethrow = mg.mark();

            mg.visitVarInsn(Opcodes.ASTORE, 1);
            mg.newInstance(uteType);
            mg.dup();
            mg.visitVarInsn(Opcodes.ALOAD, 1);
            mg.invokeConstructor(uteType,
                    Method.getMethod("void <init>(java.lang.Throwable)"));
            mg.throwException();

            mg.visitTryCatchBlock(tryBlockStart, tryBlockEnd, wrapAndRethrow, Type.getInternalName(Throwable.class));
        }

        // finish the method
        mg.endMethod();
        mg.visitMaxs(10, 10);
        mg.visitEnd();
    }

    /**
     * Generates:
     * <pre>
     * Method method =
     *      method.getDeclaringClass().getMethod("methodName", new Class[] { args... });
     * </pre>
     * @param mg
     * @param method
     * @param methodType
     */
    private static void loadCurrentMethod(GeneratorAdapter mg, java.lang.reflect.Method method, Type methodType)
    {
        mg.push(Type.getType(method.getDeclaringClass()));
        mg.push(method.getName());

        // create the Class[]
        mg.push(methodType.getArgumentTypes().length);
        mg.newArray(TYPE_CLASS);

        // push parameters into array
        for (int i = 0; i < methodType.getArgumentTypes().length; i++)
        {
            // keep copy of array on stack
            mg.dup();

            // push index onto stack
            mg.push(i);
            mg.push(methodType.getArgumentTypes()[i]);
            mg.arrayStore(TYPE_CLASS);
        }

        // invoke getMethod() with the method name and the array of types
        mg.invokeVirtual(TYPE_CLASS, Method.getMethod("java.lang.reflect.Method getDeclaredMethod(String, Class[])"));
    }

    /**
     * Defines a new Object[] and push all method argmuments into the array.
     *
     * @param mg
     * @param method
     * @param methodType
     */
    private static void loadArguments(GeneratorAdapter mg, java.lang.reflect.Method method, Type methodType)
    {
        // create the Object[]
        mg.push(methodType.getArgumentTypes().length);
        mg.newArray(TYPE_OBJECT);

        // push parameters into array
        for (int i = 0; i < methodType.getArgumentTypes().length; i++)
        {
            // keep copy of array on stack
            mg.dup();

            // push index onto stack
            mg.push(i);

            mg.loadArg(i);
            mg.valueOf(methodType.getArgumentTypes()[i]);
            mg.arrayStore(TYPE_OBJECT);
        }
    }

    private static Type[] getTypes(Class<?>... src)
    {
        Type[] result = new Type[src.length];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = Type.getType(src[i]);
        }
        return result;
    }

    /**
     * Adapted from http://asm.ow2.org/doc/faq.html#Q5
     *
     * @param b
     *
     * @return Class<?>
     */
    private static Class<?> loadClass(ClassLoader loader, String className, byte[] b)
    {
        // override classDefine (as it is protected) and define the class.
        try
        {
            java.lang.reflect.Method method = ClassLoader.class.getDeclaredMethod(
                    "defineClass", String.class, byte[].class, int.class, int.class);

            // protected method invocation
            boolean accessible = method.isAccessible();
            if (!accessible)
            {
                method.setAccessible(true);
            }
            try
            {
                return (Class<?>) method.invoke(loader, className, b, Integer.valueOf(0), Integer.valueOf(b.length));
            }
            finally
            {
                if (!accessible)
                {
                    method.setAccessible(false);
                }
            }
        }
        catch (Exception e)
        {
            throw e instanceof RuntimeException ? ((RuntimeException) e) : new RuntimeException(e);
        }
    }
}
