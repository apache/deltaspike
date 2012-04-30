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

import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.junit.Test;

import javax.enterprise.context.RequestScoped;
import java.lang.annotation.Annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
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
    public void assertCorrectAnnotationType()
    {
        assertThat(AnnotationInstanceProvider.of(RequestScoped.class), is(RequestScoped.class));
    }

    @Test
    public void assertSameInstance()
    {
        Annotation a1 = AnnotationInstanceProvider.of(RequestScoped.class);
        Annotation a2 = AnnotationInstanceProvider.of(RequestScoped.class);

        assertThat(a2, sameInstance(a1));
    }

    @Test
    public void assertSameInstanceUsingEquals()
    {
        Annotation a1 = AnnotationInstanceProvider.of(RequestScoped.class);
        Annotation a2 = AnnotationInstanceProvider.of(RequestScoped.class);

        assertTrue(a2.equals(a1));
    }
}
