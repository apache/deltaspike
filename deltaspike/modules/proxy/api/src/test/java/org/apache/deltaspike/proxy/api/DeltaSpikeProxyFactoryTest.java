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

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class DeltaSpikeProxyFactoryTest
{
    public static class DeltaSpikeProxyFactoryForCollectTest extends DeltaSpikeProxyFactory
    {
        @Override
        protected ArrayList<Method> getDelegateMethods(Class<?> targetClass, ArrayList<Method> allMethods)
        {
            return null;
        }

        @Override
        protected String getProxyClassSuffix()
        {
            return null;
        }

        public ArrayList<Method> collectAllMethodsDelegate(Class<?> clazz)
        {
           return collectAllMethods(clazz); 
        }
    }
    
    private DeltaSpikeProxyFactoryForCollectTest proxyFactory;

    @Before
    public void setUp() throws Exception
    {
        proxyFactory = new DeltaSpikeProxyFactoryForCollectTest();
    }

    public class Class_NonAbstract
    {
        public void test()
        {
        }
    }
    
    public abstract class Class_NonAbstractToAbstract extends Class_NonAbstract
    {
        public abstract void test();
    }

    public abstract static class Class_C1
    {
        protected abstract void protectedAbstract_C1_C2(); // will be overridden in Class_C2
        protected abstract void protectedAbstract_C1_C3(); // will be overridden in Class_C3
        protected abstract void protectedAbstract_C1_C2_C3(); // will be overridden in Class_C2, Class_C3
        protected abstract void protectedAbstract_C1(); // will not be overridden
        
        public abstract void publicAbstract_C1_C2(); // will be overridden in Class_C2
        public abstract void publicAbstract_C1_C3(); // will be overridden in Class_C3
        public abstract void publicAbstract_C1_C2_C3(); // will be overridden in Class_C2, Class_C3
        public abstract void publicAbstract_C1(); // will not be overriden at all
       
        public void test(List l) { }
    }
    
    public abstract static class Class_C2 extends Class_C1
    {
        protected void protectedAbstract_C1_C2() // Leave in Class_C2
        {
        }
        
        public void publicAbstract_C1_C2() // Leave in Class_C2
        {
        }
        
        protected abstract void protectedAbstract_C2_C3(); // will be overridden in Class_C3 
        protected abstract void protectedAbstract_C2(); // will not  be overridden
        
        public abstract void publicAbstract_C2_C3(); // will be overridden in Class_C3
        public abstract void publicAbstract_C2(); // will not  be overridden
        
        public abstract void test(List list);
    }
    
    public abstract static class Class_C3 extends Class_C2
    {
        public void protectedAbstract_C1_C3()
        {
        }
        
        public void publicAbstract_C1_C3()
        {
        }
        
        public void protectedAbstract_C2_C3()
        {
        }
        
        public void publicAbstract_C2_C3()
        {
        }
    }
    
    private boolean containsMethod(List<Method> collectedMethods, Class<?> declaringClass, String methodName)
    {
        for(Method m: collectedMethods)
        {
            if (m.getDeclaringClass() == declaringClass && methodName.equals(m.getName()))
            {
                return true;
            }
        }
        return false;
    }
    
    private void printCollectedMethods(List<Method> collectedMethods)
    {
        for(Method m: collectedMethods)
        {
            if (m.getDeclaringClass() != Object.class)
            {
                System.out.println(m.getDeclaringClass().getName() + " " + m.getName());
            }
        }
    }
    
    @Test
    public void testCollection_NonAbstractToAbstract()
    {
        ArrayList<Method> collectedMethods = proxyFactory.collectAllMethods(Class_NonAbstractToAbstract.class);
        printCollectedMethods(collectedMethods);
    }
    
    @Test
    public void testMethCollection_C1()
    {
        ArrayList<Method> collectedMethods = proxyFactory.collectAllMethods(Class_C1.class);
        // System.out.println("testCollectitOn0");
        // printCollectedMethods(collectedMethods);
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "protectedAbstract_C1_C2"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "protectedAbstract_C1_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "protectedAbstract_C1"));
        
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "publicAbstract_C1_C2"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "publicAbstract_C1_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "publicAbstract_C1"));
    }
    
    @Test
    public void testMethCollection_C2()
    {
        ArrayList<Method> collectedMethods = proxyFactory.collectAllMethods(Class_C2.class);
        // System.out.println("testMethCollection_C2");
        // printCollectedMethods(collectedMethods);
        assertTrue(containsMethod(collectedMethods, Class_C2.class, "protectedAbstract_C1_C2"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "protectedAbstract_C1_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "protectedAbstract_C1_C2_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "protectedAbstract_C1"));
        
        assertTrue(containsMethod(collectedMethods, Class_C2.class, "publicAbstract_C1_C2"));
        assertTrue(containsMethod(collectedMethods, Class_C2.class, "publicAbstract_C1_C2"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "publicAbstract_C1_C2_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "publicAbstract_C1"));
    
        assertTrue(containsMethod(collectedMethods, Class_C2.class, "protectedAbstract_C2_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C2.class, "protectedAbstract_C2"));
        
        assertTrue(containsMethod(collectedMethods, Class_C2.class, "publicAbstract_C2_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C2.class, "publicAbstract_C2"));
    }
    

    @Test
    public void testMethCollection_C3()
    {
        ArrayList<Method> collectedMethods = proxyFactory.collectAllMethods(Class_C3.class);
        // System.out.println("testCollectitOn02");
        // printCollectedMethods(collectedMethods);
        assertTrue(containsMethod(collectedMethods, Class_C2.class, "protectedAbstract_C1_C2"));
        assertTrue(containsMethod(collectedMethods, Class_C3.class, "protectedAbstract_C1_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "protectedAbstract_C1_C2_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "protectedAbstract_C1"));
        
        assertTrue(containsMethod(collectedMethods, Class_C2.class, "publicAbstract_C1_C2"));
        assertTrue(containsMethod(collectedMethods, Class_C3.class, "publicAbstract_C1_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "publicAbstract_C1_C2_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C1.class, "publicAbstract_C1"));
        
        assertTrue(containsMethod(collectedMethods, Class_C3.class, "protectedAbstract_C2_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C2.class, "protectedAbstract_C2"));
        
        assertTrue(containsMethod(collectedMethods, Class_C3.class, "publicAbstract_C2_C3"));
        assertTrue(containsMethod(collectedMethods, Class_C2.class, "publicAbstract_C2"));
        
    }

}
