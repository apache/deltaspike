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

package org.apache.deltaspike.test.api.metadata;

import org.apache.deltaspike.core.api.literal.AlternativeLiteral;
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.literal.ApplicationScopedLiteral;
import org.apache.deltaspike.core.api.literal.NamedLiteral;
import org.apache.deltaspike.core.api.literal.TypedLiteral;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.junit.Test;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class AnnotatedTypeBuilderTest
{
    @Test
    public void testTypeLevelAnnotationRedefinition()
    {
        AnnotatedTypeBuilder<Cat> builder = new AnnotatedTypeBuilder<Cat>();
        builder.readFromType(Cat.class);

        AnnotatedType<Cat> cat = builder.create();

        assertNotNull(cat);
        assertNotNull(cat.getAnnotation(Named.class));
        assertEquals("cat", cat.getAnnotation(Named.class).value());

        builder.addToClass(new AlternativeLiteral())
                .addToClass(new ApplicationScopedLiteral())
                .removeFromClass(Named.class)
                .addToClass(new NamedLiteral("tomcat"));

        cat = builder.create();
        assertNotNull(cat);

        assertEquals(3, cat.getAnnotations().size());
        assertTrue(cat.isAnnotationPresent(Named.class));
        assertTrue(cat.isAnnotationPresent(Alternative.class));
        assertTrue(cat.isAnnotationPresent(ApplicationScoped.class));
        assertEquals("tomcat", cat.getAnnotation(Named.class).value());
        
        AnnotatedMethod observerMethod = null;
        for (AnnotatedMethod m : cat.getMethods())
        {
            if ("doSomeObservation".equals(m.getJavaMember().getName()))
            {
                observerMethod = m;
                break;
            }
        }
        assertNotNull(observerMethod);
        observerMethod.isAnnotationPresent(Observes.class);
        
        {
            // test reading from an AnnotatedType
            AnnotatedTypeBuilder<Cat> builder2 = new AnnotatedTypeBuilder<Cat>();
            builder2.readFromType(cat);
            builder2.removeFromAll(Named.class);

            final AnnotatedType<Cat> noNameCat = builder2.create();
            assertFalse(noNameCat.isAnnotationPresent(Named.class));
            assertEquals(2, noNameCat.getAnnotations().size());
        }

        {

            // test reading from an AnnotatedType in non-overwrite mode
            AnnotatedTypeBuilder<Cat> builder3 = new AnnotatedTypeBuilder<Cat>();
            builder3.readFromType(cat, true);
            builder3.removeFromAll(Named.class);

            builder3.readFromType(cat, false);

            final AnnotatedType<Cat> namedCat = builder3.create();
            assertTrue(namedCat.isAnnotationPresent(Named.class));
            assertEquals(3, namedCat.getAnnotations().size());
        }
    }

    @Test
    public void testAdditionOfAnnotation()
    {
        final AnnotatedTypeBuilder<Cat> builder = new AnnotatedTypeBuilder<Cat>();
        builder.readFromType(Cat.class, true);
        builder.addToClass(new TypedLiteral());

        final AnnotatedType<Cat> catAnnotatedType = builder.create();
        assertThat(catAnnotatedType.isAnnotationPresent(Typed.class), is(true));
    }

    @Test
    public void modifyAnnotationsOnConstructorParameter() throws NoSuchMethodException
    {
        final AnnotatedTypeBuilder<Cat> builder = new AnnotatedTypeBuilder<Cat>();
        builder.readFromType(Cat.class, true);
        builder.removeFromConstructorParameter(Cat.class.getConstructor(String.class, String.class), 1, Default.class);
        builder.addToConstructorParameter(Cat.class.getConstructor(String.class, String.class), 1, new AnyLiteral());

        final AnnotatedType<Cat> catAnnotatedType = builder.create();
        Set<AnnotatedConstructor<Cat>> catCtors = catAnnotatedType.getConstructors();

        assertThat(catCtors.size(), is(2));

        for (AnnotatedConstructor<Cat> ctor : catCtors)
        {
            if (ctor.getParameters().size() == 2)
            {
                List<AnnotatedParameter<Cat>> ctorParams = ctor.getParameters();

                assertThat(ctorParams.get(1).getAnnotations().size(), is(1));
                assertThat((AnyLiteral) ctorParams.get(1).getAnnotations().toArray()[0], is(new AnyLiteral()));
            }
        }
    }

    @Test
    public void buildValidAnnotationAnnotatedType()
    {
        final AnnotatedTypeBuilder<Small> builder = new AnnotatedTypeBuilder<Small>();
        builder.readFromType(Small.class);
        final AnnotatedType<Small> smallAnnotatedType = builder.create();

        assertThat(smallAnnotatedType.getMethods().size(), is(1));
        assertThat(smallAnnotatedType.getConstructors().size(), is(0));
        assertThat(smallAnnotatedType.getFields().size(), is(0));
    }


    @Test
    public void testCtWithMultipleParams()
    {
        final AnnotatedTypeBuilder<TypeWithParamsInCt> builder = new AnnotatedTypeBuilder<TypeWithParamsInCt>();
        builder.readFromType(TypeWithParamsInCt.class);
        builder.addToClass(new AnnotationLiteral<Default>() {});

        AnnotatedType<TypeWithParamsInCt> newAt = builder.create();
        assertNotNull(newAt);
    }

    @Test
    public void testEnumWithParam()
    {
        final AnnotatedTypeBuilder<EnumWithParams> builder = new AnnotatedTypeBuilder<EnumWithParams>();
        builder.readFromType(EnumWithParams.class);
        builder.addToClass(new AnnotationLiteral<Default>() {});

        AnnotatedType<EnumWithParams> newAt = builder.create();
        assertNotNull(newAt);
    }


    public static class TypeWithParamsInCt
    {
        public TypeWithParamsInCt(String a, int b, String c)
        {
            // all fine
        }
    }

    public enum EnumWithParams
    {
        VALUE("A");

        EnumWithParams(String val) {
            // all fine
        }
    }

    @Test
    public void testExceptionPerformance() {
        long start = System.nanoTime();
        long val = -230349823423L;
        Exception e = new Exception("static");
        for (int i=0; i < 10_000_000; i++) {
            try {
                val += 19;
                throw e;
            }
            catch (Exception e2) {
                // do nothing
            }
        }
        long end = System.nanoTime();
        System.out.println("Exeptions took ms " + TimeUnit.NANOSECONDS.toMillis(end - start));
    }

}
