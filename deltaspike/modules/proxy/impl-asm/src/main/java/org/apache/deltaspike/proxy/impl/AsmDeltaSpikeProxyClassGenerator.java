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
package org.apache.deltaspike.proxy.impl;

import org.apache.deltaspike.proxy.spi.DeltaSpikeProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;

import javax.enterprise.inject.Typed;

import org.apache.deltaspike.proxy.spi.invocation.DeltaSpikeProxyInvocationHandler;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.apache.deltaspike.proxy.spi.DeltaSpikeProxyClassGenerator;

@Typed
public class AsmDeltaSpikeProxyClassGenerator implements DeltaSpikeProxyClassGenerator
{
    private static final String FIELDNAME_INVOCATION_HANDLER = "invocationHandler";
    private static final String FIELDNAME_DELEGATE_INVOCATION_HANDLER = "delegateInvocationHandler";
    private static final String FIELDNAME_DELEGATE_METHODS = "delegateMethods";

    private static final Type TYPE_CLASS = Type.getType(Class.class);
    private static final Type TYPE_OBJECT = Type.getType(Object.class);
    private static final Type TYPE_DELTA_SPIKE_PROXY_INVOCATION_HANDLER =
            Type.getType(DeltaSpikeProxyInvocationHandler.class);
    private static final Type TYPE_METHOD_ARRAY = Type.getType(java.lang.reflect.Method[].class);
    private static final Type TYPE_INVOCATION_HANDLER = Type.getType(InvocationHandler.class);

    @Override
    public <T> Class<T> generateProxyClass(ClassLoader classLoader,
            Class<T> targetClass,
            String suffix,
            String superAccessorMethodSuffix,
            Class<?>[] additionalInterfaces,
            java.lang.reflect.Method[] delegateMethods,
            java.lang.reflect.Method[] interceptMethods)
    {
        String proxyName = targetClass.getName() + suffix;
        String classFileName = proxyName.replace('.', '/');

        byte[] proxyBytes = generateProxyClassBytes(targetClass,
                classFileName, superAccessorMethodSuffix, additionalInterfaces, delegateMethods, interceptMethods);

        Class<T> proxyClass = (Class<T>) loadClass(classLoader, proxyName, proxyBytes,
                targetClass.getProtectionDomain());

        return proxyClass;
    }

    private static byte[] generateProxyClassBytes(Class<?> targetClass,
            String proxyName,
            String superAccessorMethodSuffix,
            Class<?>[] additionalInterfaces,
            java.lang.reflect.Method[] delegateMethods,
            java.lang.reflect.Method[] interceptMethods)
    {
        Class<?> superClass = targetClass;
        String[] interfaces = new String[] { };

        if (targetClass.isInterface())
        {
            superClass = Object.class;
            interfaces = new String[] { Type.getInternalName(targetClass) };
        }

        // add DeltaSpikeProxy as interface
        interfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
        interfaces[interfaces.length - 1] = Type.getInternalName(DeltaSpikeProxy.class);

        if (additionalInterfaces != null && additionalInterfaces.length > 0)
        {
            interfaces = Arrays.copyOf(interfaces, interfaces.length + additionalInterfaces.length);
            for (int i = 0; i < additionalInterfaces.length; i++)
            {
                interfaces[(interfaces.length - 1) + i] = Type.getInternalName(additionalInterfaces[i]);
            }
        }

        Type superType = Type.getType(superClass);
        Type proxyType = Type.getObjectType(proxyName);

        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, proxyType.getInternalName(), null,
                superType.getInternalName(), interfaces);

        
        defineDefaultConstructor(cw, proxyType, superType);
        defineDeltaSpikeProxyFields(cw);
        defineDeltaSpikeProxyMethods(cw, proxyType);

        if (delegateMethods != null)
        {
            for (java.lang.reflect.Method method : delegateMethods)
            {
                defineMethod(cw, method, proxyType);
            }
        }

        if (interceptMethods != null)
        {
            for (java.lang.reflect.Method method : interceptMethods)
            {
                defineSuperAccessorMethod(cw, method, superType, superAccessorMethodSuffix);
                defineMethod(cw, method, proxyType);
            }
        }

        // copy all annotations from the source class
        try
        {
            // ClassVisitor to intercept all annotation visits on the class
            ClassVisitor cv = new ClassVisitor(Opcodes.ASM7)
            {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible)
                {
                    return new CopyAnnotationVisitorAdapter(
                            super.visitAnnotation(desc, visible),
                            cw.visitAnnotation(desc, visible));
                }
            };

            // visit class to proxy with our visitor to copy all annotations from the source class to our ClassWriter
            String sourceClassFilename = targetClass.getName().replace('.', '/') + ".class";
            ClassReader cr = new ClassReader(targetClass.getClassLoader().getResourceAsStream(sourceClassFilename));
            cr.accept(cv, 0);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return cw.toByteArray();
    }
    
    private static void defineDefaultConstructor(ClassWriter cw, Type proxyType, Type superType)
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
    
    private static void defineDeltaSpikeProxyFields(ClassWriter cw)
    {
        // generates
        // private DeltaSpikeProxyInvocationHandler invocationHandler;
        cw.visitField(Opcodes.ACC_PRIVATE, FIELDNAME_INVOCATION_HANDLER,
                TYPE_DELTA_SPIKE_PROXY_INVOCATION_HANDLER.getDescriptor(), null, null).visitEnd();
        
        // generates
        // private MyInvocationHandler delegateInvocationHandler;
        cw.visitField(Opcodes.ACC_PRIVATE, FIELDNAME_DELEGATE_INVOCATION_HANDLER,
                TYPE_INVOCATION_HANDLER.getDescriptor(), null, null).visitEnd();
        
        // generates
        // private Method[] delegateMethods;
        cw.visitField(Opcodes.ACC_PRIVATE, FIELDNAME_DELEGATE_METHODS,
                TYPE_METHOD_ARRAY.getDescriptor(), null, null).visitEnd();
    }

    private static void defineDeltaSpikeProxyMethods(ClassWriter cw, Type proxyType)
    {
        try
        {
            // implement #setInvocationHandler
            Method asmMethod = Method.getMethod(DeltaSpikeProxy.class.getDeclaredMethod(
                    "setInvocationHandler", DeltaSpikeProxyInvocationHandler.class));
            GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, asmMethod, null, null, cw);

            mg.visitCode();

            mg.loadThis();
            mg.loadArg(0);
            mg.checkCast(TYPE_DELTA_SPIKE_PROXY_INVOCATION_HANDLER);
            mg.putField(proxyType, FIELDNAME_INVOCATION_HANDLER, TYPE_DELTA_SPIKE_PROXY_INVOCATION_HANDLER);
            mg.returnValue();

            mg.visitMaxs(2, 1);
            mg.visitEnd();


            // implement #getInvocationHandler
            asmMethod = Method.getMethod(DeltaSpikeProxy.class.getDeclaredMethod("getInvocationHandler"));
            mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, asmMethod, null, null, cw);

            mg.visitCode();

            mg.loadThis();
            mg.getField(proxyType, FIELDNAME_INVOCATION_HANDLER, TYPE_DELTA_SPIKE_PROXY_INVOCATION_HANDLER);
            mg.returnValue();

            mg.visitMaxs(2, 1);
            mg.visitEnd();
            
            
            
            
            // implement #setDelegateInvocationHandler
            asmMethod = Method.getMethod(DeltaSpikeProxy.class.getDeclaredMethod(
                    "setDelegateInvocationHandler", InvocationHandler.class));
            mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, asmMethod, null, null, cw);

            mg.visitCode();

            mg.loadThis();
            mg.loadArg(0);
            mg.checkCast(TYPE_INVOCATION_HANDLER);
            mg.putField(proxyType, FIELDNAME_DELEGATE_INVOCATION_HANDLER, TYPE_INVOCATION_HANDLER);
            mg.returnValue();

            mg.visitMaxs(2, 1);
            mg.visitEnd();


            // implement #getDelegateInvocationHandler
            asmMethod = Method.getMethod(DeltaSpikeProxy.class.getDeclaredMethod("getDelegateInvocationHandler"));
            mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, asmMethod, null, null, cw);

            mg.visitCode();

            mg.loadThis();
            mg.getField(proxyType, FIELDNAME_DELEGATE_INVOCATION_HANDLER, TYPE_INVOCATION_HANDLER);
            mg.returnValue();

            mg.visitMaxs(2, 1);
            mg.visitEnd();
            
            
            
            
            // implement #setDelegateMethods
            asmMethod = Method.getMethod(DeltaSpikeProxy.class.getDeclaredMethod(
                    "setDelegateMethods", java.lang.reflect.Method[].class));
            mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, asmMethod, null, null, cw);

            mg.visitCode();

            mg.loadThis();
            mg.loadArg(0);
            mg.checkCast(TYPE_METHOD_ARRAY);
            mg.putField(proxyType, FIELDNAME_DELEGATE_METHODS, TYPE_METHOD_ARRAY);
            mg.returnValue();

            mg.visitMaxs(2, 1);
            mg.visitEnd();


            // implement #getDelegateMethods
            asmMethod = Method.getMethod(DeltaSpikeProxy.class.getDeclaredMethod("getDelegateMethods"));
            mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, asmMethod, null, null, cw);

            mg.visitCode();

            mg.loadThis();
            mg.getField(proxyType, FIELDNAME_DELEGATE_METHODS, TYPE_METHOD_ARRAY);
            mg.returnValue();

            mg.visitMaxs(2, 1);
            mg.visitEnd();
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalStateException("Unable to implement " + DeltaSpikeProxy.class.getName(), e);
        }
    }

    private static void defineSuperAccessorMethod(ClassWriter cw, java.lang.reflect.Method method, Type superType,
            String superAccessorMethodSuffix) 
    {
        Method originalAsmMethod = Method.getMethod(method);
        Method newAsmMethod = new Method(method.getName() + superAccessorMethodSuffix,
                originalAsmMethod.getReturnType(),
                originalAsmMethod.getArgumentTypes());
        GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, newAsmMethod, null, null, cw);
        
        mg.visitCode();
        
        // call super method
        mg.loadThis();
        mg.loadArgs();
        mg.visitMethodInsn(Opcodes.INVOKESPECIAL,
                superType.getInternalName(),
                method.getName(),
                Type.getMethodDescriptor(method),
                false);
        mg.returnValue();
        
        // finish the method
        mg.endMethod();
        mg.visitMaxs(10, 10);
        mg.visitEnd();
    }
    
    private static void defineMethod(ClassWriter cw, java.lang.reflect.Method method, Type proxyType)
    {
        Type methodType = Type.getType(method);
        
        ArrayList<Type> exceptionsToCatch = new ArrayList<Type>();
        for (Class<?> exception : method.getExceptionTypes())
        {
            if (!RuntimeException.class.isAssignableFrom(exception))
            {
                exceptionsToCatch.add(Type.getType(exception));
            }
        }
        
        // push the method definition
        int modifiers = (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED) & method.getModifiers();
        Method asmMethod = Method.getMethod(method);
        GeneratorAdapter mg = new GeneratorAdapter(modifiers,
                asmMethod,
                null,
                getTypes(method.getExceptionTypes()),
                cw);

        // copy annotations
        for (Annotation annotation : method.getDeclaredAnnotations())
        {
            mg.visitAnnotation(Type.getDescriptor(annotation.annotationType()), true).visitEnd();
        }

        mg.visitCode();

        Label tryBlockStart = mg.mark();

        mg.loadThis();
        mg.getField(proxyType, FIELDNAME_INVOCATION_HANDLER, TYPE_DELTA_SPIKE_PROXY_INVOCATION_HANDLER);
        mg.loadThis();
        loadCurrentMethod(mg, method, methodType);
        loadArguments(mg, method, methodType);
        
        mg.invokeVirtual(TYPE_DELTA_SPIKE_PROXY_INVOCATION_HANDLER,
                Method.getMethod("Object invoke(Object, java.lang.reflect.Method, Object[])"));

        // cast the result
        mg.unbox(methodType.getReturnType());

        // build try catch
        Label tryBlockEnd = mg.mark();
        
        // push return
        mg.returnValue();

        // catch runtime exceptions and rethrow it
        Label rethrow = mg.mark();
        mg.visitVarInsn(Opcodes.ASTORE, 1);
        mg.visitVarInsn(Opcodes.ALOAD, 1);
        mg.throwException();
        mg.visitTryCatchBlock(tryBlockStart, tryBlockEnd, rethrow, Type.getInternalName(RuntimeException.class));

        // catch checked exceptions and rethrow it
        boolean throwableCatched = false;
        if (!exceptionsToCatch.isEmpty())
        {
            rethrow = mg.mark();
            mg.visitVarInsn(Opcodes.ASTORE, 1);
            mg.visitVarInsn(Opcodes.ALOAD, 1);
            mg.throwException();

            // catch declared exceptions and rethrow it...
            for (Type exceptionType : exceptionsToCatch)
            {
                if (exceptionType.getClassName().equals(Throwable.class.getName()))
                {
                    throwableCatched = true;
                }
                mg.visitTryCatchBlock(tryBlockStart, tryBlockEnd, rethrow, exceptionType.getInternalName());
            }
        }

        // if throwable isn't alreached cachted, catch it and wrap it with an UndeclaredThrowableException and throw it
        if (!throwableCatched)
        {
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
        mg.visitMaxs(12, 12);
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
    private static Class<?> loadClass(ClassLoader loader, String className, byte[] b,
            ProtectionDomain protectionDomain)
    {
        // override classDefine (as it is protected) and define the class.
        try
        {
            java.lang.reflect.Method method = ClassLoader.class.getDeclaredMethod(
                    "defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);

            // protected method invocation
            boolean accessible = method.isAccessible();
            if (!accessible)
            {
                method.setAccessible(true);
            }
            try
            {
                return (Class<?>) method.invoke(loader, className, b, Integer.valueOf(0), Integer.valueOf(b.length),
                        protectionDomain);
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
