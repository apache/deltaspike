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
package org.apache.deltaspike.core.impl.util;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.reflect.Method;

public final class AnnotatedMethods
{
    private AnnotatedMethods()
    {
        // no-op
    }

    public static AnnotatedMethod<?> findMethod(final AnnotatedType<?> type, final Method method)
    {
        AnnotatedMethod<?> annotatedMethod = null;
        for (final AnnotatedMethod<?> am : type.getMethods())
        {
            if (am.getJavaMember().equals(method))
            {
                annotatedMethod = am;
                break;
            }
        }
        if (annotatedMethod == null)
        {
            throw new IllegalStateException("No annotated method for " + method);
        }
        return annotatedMethod;
    }
}
