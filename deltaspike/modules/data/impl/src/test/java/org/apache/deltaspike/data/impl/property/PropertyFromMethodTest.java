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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.net.URL;

import org.apache.deltaspike.data.impl.property.MethodProperty;
import org.apache.deltaspike.data.impl.property.Properties;
import org.apache.deltaspike.data.impl.property.Property;
import org.junit.Test;

/**
 * Verify that only valid properties are permitted, as per the JavaBean specification.
 * @see http://www.oracle.com/technetwork/java/javase/documentation/spec-136004.html
 */
public class PropertyFromMethodTest
{

    @Test
    public void testValidPropertyGetterMethod() throws Exception
    {
        Method getter = ClassToIntrospect.class.getMethod("getName");
        Property<String> p = Properties.createProperty(getter);
        assertNotNull(p);
        assertEquals("name", p.getName());
        assertEquals(getter, p.getMember());
    }

    @Test
    public void testValidPropertySetterMethod() throws Exception
    {
        Property<String> p = Properties.createProperty(ClassToIntrospect.class.getMethod("setName", String.class));
        assertNotNull(p);
        assertEquals("name", p.getName());
    }

    @Test
    public void testReadOnlyProperty() throws Exception
    {
        Property<String> p = Properties.createProperty(ClassToIntrospect.class.getMethod("getTitle"));
        assertNotNull(p);
        assertEquals("title", p.getName());
        assertTrue(p.isReadOnly());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPropertyGetterMethod() throws Exception
    {
        Properties.createProperty(ClassToIntrospect.class.getMethod("get"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyBooleanPropertyGetterMethod() throws Exception
    {
        Properties.createProperty(ClassToIntrospect.class.getMethod("is"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonPrimitiveBooleanPropertyIsMethod() throws Exception
    {
        Properties.createProperty(ClassToIntrospect.class.getMethod("isValid"));
    }

    @Test
    public void testSingleCharPropertyGetterMethod() throws Exception
    {
        Method getter = ClassToIntrospect.class.getMethod("getP");
        Property<String> p = Properties.createProperty(getter);
        assertNotNull(p);
        assertEquals("p", p.getName());
        assertEquals(getter, p.getMember());
    }

    @Test
    public void testSingleCharPropertySetterMethod() throws Exception
    {
        Property<String> p = Properties.createProperty(ClassToIntrospect.class.getMethod("setP", String.class));
        assertNotNull(p);
        assertEquals("p", p.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetterMethodWithVoidReturnType() throws Exception
    {
        Properties.createProperty(ClassToIntrospect.class.getMethod("getFooBar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetterMethodWithMultipleParameters() throws Exception
    {
        Properties.createProperty(ClassToIntrospect.class.getMethod("setSalary", Double.class, Double.class));
    }

    @Test
    public void testAcronymProperty() throws Exception
    {
        Method getter = ClassToIntrospect.class.getMethod("getURL");
        Property<URL> p = Properties.createProperty(getter);
        assertNotNull(p);
        assertEquals("URL", p.getName());
        assertEquals(getter, p.getMember());
    }

    // SOLDER-298
    @Test
    public void testPrimitiveBooleanProperty() throws Exception
    {
        Property<Boolean> p = Properties.createProperty(ClassToIntrospect.class.getMethod("isValidPrimitiveBoolean"));

        assertNotNull(p);
    }

    @Test
    public void testAccessingPrimitiveTypedMethodProperty() throws Exception
    {
        final Method method = ClassToIntrospect.class.getMethod("getPrimitiveProperty");

        MethodProperty<Object> propertyUT = Properties.createProperty(method);
        propertyUT.getValue(new ClassToIntrospect());
    }
}
