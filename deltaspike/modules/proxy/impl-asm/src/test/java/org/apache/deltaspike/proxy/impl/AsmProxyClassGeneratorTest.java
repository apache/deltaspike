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

import javax.inject.Named;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AsmProxyClassGeneratorTest
{
    private static Class<? extends TestClass> proxyClass;
    
    @BeforeClass
    public static void init()
    {
        AsmDeltaSpikeProxyClassGenerator asmProxyClassGenerator = new AsmDeltaSpikeProxyClassGenerator();
        proxyClass = asmProxyClassGenerator.generateProxyClass(TestClass.class.getClassLoader(),
                TestClass.class,
                "$Test",
                "$super",
                null,
                null,
                null); 
    }
    
    @Test
    public void testCopyAnnotationValues()
    {
        Assert.assertEquals(
                TestClass.class.getAnnotations().length,
                proxyClass.getAnnotations().length);
        
        Assert.assertEquals(
                TestClass.class.getAnnotation(Named.class).value(),
                proxyClass.getAnnotation(Named.class).value());
        
        Assert.assertEquals(
                TestClass.class.getAnnotation(TestAnnotation.class).value1(),
                proxyClass.getAnnotation(TestAnnotation.class).value1());
        Assert.assertEquals(
                TestClass.class.getAnnotation(TestAnnotation.class).value2(),
                proxyClass.getAnnotation(TestAnnotation.class).value2());
        Assert.assertEquals(
                TestClass.class.getAnnotation(TestAnnotation.class).value3(),
                proxyClass.getAnnotation(TestAnnotation.class).value3());
    }
}
