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
package org.apache.deltaspike.testcontrol.impl.validation;

import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.spi.TestAware;
import org.apache.deltaspike.testcontrol.spi.TestControlValidator;

import javax.enterprise.inject.Typed;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Typed()
public class StandardContextTestControlValidator implements TestAware, TestControlValidator
{
    private static Boolean customContextControlDetected;

    private static ThreadLocal<Class> currentTestClass = new ThreadLocal<Class>();
    private static ThreadLocal<Method> currentTestMethod = new ThreadLocal<Method>();

    @Override
    public void validate(TestControl testControl)
    {
        checkActiveContextControlImplementation();

        List<Class<? extends Annotation>> scopeClasses = new ArrayList<Class<? extends Annotation>>();
        Collections.addAll(scopeClasses, testControl.startScopes());

        validateSupportedScopes(scopeClasses, currentTestClass.get(), currentTestMethod.get());
    }

    private void checkActiveContextControlImplementation()
    {
        if (customContextControlDetected != null)
        {
            return;
        }

        customContextControlDetected = !CdiContainerLoader.getCdiContainer().getContextControl()
            .getClass().getName().startsWith("org.apache.deltaspike.");
    }

    private void validateSupportedScopes(List<Class<? extends Annotation>> scopeClasses,
                                         Class<?> declaringClass,
                                         Method testMethod)
    {
        //skip validation in case of a custom context-control implementation (it might support more scopes)
        if (Boolean.TRUE.equals(customContextControlDetected))
        {
            return;
        }

        for (Class<? extends Annotation> scopeClass : scopeClasses)
        {
            if (!scopeClass.getName().startsWith("javax.enterprise.context."))
            {
                throw new IllegalStateException("Please remove " + scopeClass.getName() + " at " + declaringClass +
                        (testMethod != null ? "#" + testMethod.getName() : "") +
                        " from @" + TestControl.class.getName() + ". @" + TestControl.class.getName() +
                        " only supports standard Scope-Annotations provided by the CDI-Specification. " +
                        "Other Contexts start automatically or need to get started with a specific Management-API. " +
                        "Examples: " +
                        "@TransactionScoped gets started automatically once the @Transactional-Interceptor is used. " +
                        "Whereas @WindowScoped starts once WindowContext#activateWindow gets called.");
            }
        }
    }

    @Override
    public void setTestClass(Class testClass)
    {
        currentTestClass.set(testClass);
        if (testClass == null)
        {
            currentTestClass.remove();
        }
    }

    @Override
    public void setTestMethod(Method testMethod)
    {
        currentTestMethod.set(testMethod);
        if (testMethod == null)
        {
            currentTestMethod.remove();
        }
    }
}
