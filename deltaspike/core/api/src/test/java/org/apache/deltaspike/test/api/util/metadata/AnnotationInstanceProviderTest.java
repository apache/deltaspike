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

package org.apache.deltaspike.test.api.util.metadata;

import org.apache.deltaspike.core.api.literal.NamedLiteral;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.junit.Test;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class AnnotationInstanceProviderTest
{
    @Test
    public void assertBasicCreation()
    {
        Annotation a = AnnotationInstanceProvider.of(RequestScoped.class);
        assertThat(a, is(notNullValue()));
    }

    @Test
    public void assertCreationWithMemberValue()
    {
        Map<String, String> memberValues = new HashMap<String, String>();
        memberValues.put("value", "test");

        Annotation a = AnnotationInstanceProvider.of(Named.class, memberValues);
        assertThat(a, is(notNullValue()));
    }

    @Test
    public void assertCorrectAnnotationType()
    {
        assertThat(AnnotationInstanceProvider.of(RequestScoped.class), is(RequestScoped.class));
    }

    @Test
    public void assertAnnotationTypeWorks()
    {
        assertEquals(AnnotationInstanceProvider.of(RequestScoped.class).annotationType(), RequestScoped.class);
    }

    @Test
    public void assertDifferentInstanceWithDifferentMembers()
    {
        Map<String, String> memberValues = new HashMap<String, String>();
        memberValues.put("value", "test");

        Map<String, String> memberValues2 = new HashMap<String, String>();
        memberValues2.put("value", "test2");

        Annotation a1 = AnnotationInstanceProvider.of(Named.class, memberValues);
        Annotation a2 = AnnotationInstanceProvider.of(Named.class, memberValues2);

        assertFalse(a2 == a1);
    }

    @Test
    public void assertSameUsingEquals()
    {
        Annotation a1 = AnnotationInstanceProvider.of(RequestScoped.class);
        Annotation a2 = AnnotationInstanceProvider.of(RequestScoped.class);

        assertTrue(a2.equals(a1));
    }

    @Test
    public void assertDifferentInstanceWithMembersUsingEquals()
    {
        Map<String, String> memberValues = new HashMap<String, String>();
        memberValues.put("value", "test");

        Annotation a1 = AnnotationInstanceProvider.of(Named.class, memberValues);
        Annotation a2 = AnnotationInstanceProvider.of(Named.class);

        assertThat(a2, not(sameInstance(a1)));
        assertFalse(a2.equals(a1));
    }

    @Test
    public void assertMemberAccessIsCorrect()
    {
        Map<String, String> memberValues = new HashMap<String, String>();
        memberValues.put("value", "test");

        Named a1 = AnnotationInstanceProvider.of(Named.class, memberValues);

        assertThat(a1.value(), is("test"));
    }

    @Test
    public void assertBasicHashCode()
    {
        assertThat(AnnotationInstanceProvider.of(RequestScoped.class).hashCode(), is(0));
    }

    @Test
    public void assertSimpleMemberHashCode()
    {
        Map<String, String> memberValues = new HashMap<String, String>();
        memberValues.put("value", "test");
        assertThat(AnnotationInstanceProvider.of(Named.class, memberValues).hashCode(), is(not(0)));
    }

    @Test
    public void assertBasicToString()
    {
        assertThat(AnnotationInstanceProvider.of(RequestScoped.class).toString(), is("@javax.enterprise.context.RequestScoped()"));
    }

    @Test
    public void assertComplexCreation()
    {
        Map<String, Object> memberValues = new HashMap<String, Object>();
        memberValues.put("booleanValue", false);
        memberValues.put("booleanValues", new boolean[]{false});
        memberValues.put("byteValue", (byte) 0);
        memberValues.put("byteValues", new byte[]{(byte) 0});
        memberValues.put("charValue", (char) 0);
        memberValues.put("charValues", new char[]{(char) 0});
        memberValues.put("doubleValue", 0.0);
        memberValues.put("doubleValues", new double[]{0.0});
        memberValues.put("floatValue", (float) 0);
        memberValues.put("floatValues", new float[]{(float) 0});
        memberValues.put("intValue", 0);
        memberValues.put("intValues", new int[]{0});
        memberValues.put("longValue", 0L);
        memberValues.put("longValues", new long[]{0L});
        memberValues.put("shortValue", 0);
        memberValues.put("shortValues", new int[]{0});
        memberValues.put("stooge", Stooge.SHEMP);
        memberValues.put("stooges", new Stooge[]{Stooge.MOE, Stooge.LARRY, Stooge.CURLY});
        memberValues.put("string", "");
        memberValues.put("strings", new String[]{""});
        memberValues.put("type", Object.class);
        memberValues.put("types", new Class[]{Object.class});
        memberValues.put("nest", AnnotationInstanceProvider.of(NestAnnotation.class, Collections.unmodifiableMap(memberValues)));

        assertThat(AnnotationInstanceProvider.of(TestAnnotation.class, memberValues), is(notNullValue()));
    }

    @Test
    public void assertComplexHashCode()
    {
        Map<String, Object> memberValues = new HashMap<String, Object>();
        memberValues.put("booleanValue", false);
        memberValues.put("booleanValues", new boolean[]{false});
        memberValues.put("byteValue", (byte) 0);
        memberValues.put("byteValues", new byte[]{(byte) 0});
        memberValues.put("charValue", (char) 0);
        memberValues.put("charValues", new char[]{(char) 0});
        memberValues.put("doubleValue", 0.0);
        memberValues.put("doubleValues", new double[]{0.0});
        memberValues.put("floatValue", (float) 0);
        memberValues.put("floatValues", new float[]{(float) 0});
        memberValues.put("intValue", 0);
        memberValues.put("intValues", new int[]{0});
        memberValues.put("longValue", 0L);
        memberValues.put("longValues", new long[]{0L});
        memberValues.put("shortValue", (short) 0);
        memberValues.put("shortValues", new short[]{(short) 0});
        memberValues.put("stooge", Stooge.SHEMP);
        memberValues.put("stooges", new Stooge[]{Stooge.MOE, Stooge.LARRY, Stooge.CURLY});
        memberValues.put("string", "");
        memberValues.put("strings", new String[]{""});
        memberValues.put("type", Object.class);
        memberValues.put("types", new Class[]{Object.class});

        Map<String, Object> nestMemberValues = new HashMap<String, Object>(memberValues);
        memberValues.put("nest", AnnotationInstanceProvider.of(NestAnnotation.class, nestMemberValues));
        memberValues.put("nests", new NestAnnotation[]{AnnotationInstanceProvider.of(NestAnnotation.class, nestMemberValues)});

        assertThat(AnnotationInstanceProvider.of(TestAnnotation.class, memberValues).hashCode(), is(not(0)));
    }

    @Test
    public void assertComplexToString()
    {
        Map<String, Object> memberValues = new HashMap<String, Object>();
        memberValues.put("booleanValue", false);
        memberValues.put("booleanValues", new boolean[]{false});
        memberValues.put("byteValue", (byte) 0);
        memberValues.put("byteValues", new byte[]{(byte) 0});
        memberValues.put("charValue", (char) 0);
        memberValues.put("charValues", new char[]{(char) 0});
        memberValues.put("doubleValue", 0.0);
        memberValues.put("doubleValues", new double[]{0.0});
        memberValues.put("floatValue", (float) 0);
        memberValues.put("floatValues", new float[]{(float) 0});
        memberValues.put("intValue", 0);
        memberValues.put("intValues", new int[] { 0 } );
        memberValues.put("longValue", 0L);
        memberValues.put("longValues", new long[] { 0L } ) ;
        memberValues.put("shortValue", (short) 0);
        memberValues.put("shortValues", new short[] { (short) 0 } );
        memberValues.put("stooge", Stooge.SHEMP);
        memberValues.put("stooges", new Stooge[] { Stooge.MOE, Stooge.LARRY, Stooge.CURLY } );
        memberValues.put("string", "");
        memberValues.put("strings", new String[] { "" } );
        memberValues.put("type", Object.class);
        memberValues.put("types", new Class[]{Object.class});

        Map<String, Object> nestMemberValues = new HashMap<String, Object>(memberValues);
        memberValues.put("nest", AnnotationInstanceProvider.of(NestAnnotation.class, nestMemberValues));
        memberValues.put("nests", new NestAnnotation[] { AnnotationInstanceProvider.of(NestAnnotation.class,
                nestMemberValues) } );

        TestAnnotation testAnnotation = AnnotationInstanceProvider.of(TestAnnotation.class, memberValues);
        String testAnnotationToString = testAnnotation.toString();

        assertTrue(testAnnotationToString.startsWith("@org.apache.deltaspike.test.api.util.metadata.TestAnnotation("));
        //assertTrue(testAnnotationToString.contains("type=class java.lang.Object,booleanValue=false,byteValue=0"));
        // Order depends on the JVM (Sun, Oracle JRockit, ...)
        for (String key : memberValues.keySet()) {
            assertTrue(testAnnotationToString.contains(key+"="));
        }

        assertTrue(testAnnotationToString.contains("type=class java.lang.Object"));
        assertTrue(testAnnotationToString.contains("booleanValue=false"));
        assertTrue(testAnnotationToString.contains("byteValue=0"));
        // End changed test
        assertTrue(testAnnotationToString.contains("nest=@org.apache.deltaspike.test.api.util.metadata.NestAnnotation"));
        assertTrue(testAnnotationToString.endsWith(")"));
    }

    @Test
    public void assertDifferentAnnotationsNotEqual()
    {
        RequestScoped annotation1 = AnnotationInstanceProvider.of(RequestScoped.class);
        Named annotation2 = AnnotationInstanceProvider.of(Named.class);

        assertFalse(annotation1.equals(annotation2));
        assertFalse(annotation2.equals(annotation1));
    }

    @Test
    public void assertCreatedAnnotationEqualToLiteral()
    {
        Map<String, String> memberValue = new HashMap<String, String>();
        memberValue.put("value", "test");

        Named named1 = AnnotationInstanceProvider.of(Named.class, memberValue);
        Named named2 = new NamedLiteral("test");

        assertTrue(named2.equals(named1));
        assertTrue(named1.equals(named2));
    }

    @Test
    public void assertCreatedAnnotationNotEqualToLiteralWithDifferentMemberValues()
    {
        Map<String, String> memberValue = new HashMap<String, String>();
        memberValue.put("value", "test1");

        Named named1 = AnnotationInstanceProvider.of(Named.class, memberValue);
        Named named2 = new NamedLiteral("test");

        assertFalse(named1.equals(named2));
    }

    @Test
    public void assertNotEqualToOtherObjects()
    {
        assertFalse(AnnotationInstanceProvider.of(Named.class).equals(""));
    }

    @Test
    public void assertHashCodeSameAsLiteral()
    {
        Named a1 = AnnotationInstanceProvider.of(Named.class);
        Named a2 = new NamedLiteral();

        assertThat(a2.hashCode(), is(a1.hashCode()));
    }
}
