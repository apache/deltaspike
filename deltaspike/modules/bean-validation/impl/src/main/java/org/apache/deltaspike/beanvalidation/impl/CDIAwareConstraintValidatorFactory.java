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
package org.apache.deltaspike.beanvalidation.impl;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.Validation;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ReflectionUtils;

/**
 * A factory for creating CDI Aware/Enabled ConstraintValidators.
 * 
 */
public class CDIAwareConstraintValidatorFactory implements
        ConstraintValidatorFactory
{
    private static final String RELEASE_INSTANCE_METHOD_NAME = "releaseInstance";
    private static volatile Boolean releaseInstanceMethodFound;
    private static Method releaseInstanceMethod;

    private final Logger log = Logger
            .getLogger(CDIAwareConstraintValidatorFactory.class.toString());
    
    private final ConstraintValidatorFactory delegate;

    public CDIAwareConstraintValidatorFactory()
    {
        delegate = Validation.byDefaultProvider().configure().getDefaultConstraintValidatorFactory();
        if (log.isLoggable(Level.CONFIG))
        {
            log.config("Setting up delegate ConstraintValidatorFactory as " + 
                    delegate.getClass().getCanonicalName());
        }
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> validatorClass)
    {
        T resolvedInst = BeanProvider.getContextualReference(validatorClass, true);
        if (resolvedInst == null)
        {
            if (log.isLoggable(Level.CONFIG))
            {
                log.config("No contextual instances found for class " + validatorClass.getCanonicalName() +
                         " delegating to DefaultProvider behavior.");
            }
            resolvedInst = this.delegate.getInstance(validatorClass);
        }
        return resolvedInst;
    }

    //BV v1.1+
    public void releaseInstance(ConstraintValidator<?, ?> constraintValidator)
    {
        if (releaseInstanceMethodFound == null)
        {
            lazyInit();
        }
        if (Boolean.TRUE.equals(releaseInstanceMethodFound))
        {
            ReflectionUtils.invokeMethod(this.delegate, releaseInstanceMethod, Void.class, true, constraintValidator);
        }
    }

    private synchronized void lazyInit()
    {
        if (releaseInstanceMethodFound != null)
        {
            return;
        }

        Class<?> currentClass = delegate.getClass();
        while (currentClass != null && !Object.class.getName().equals(currentClass.getName()))
        {
            for (Method currentMethod : currentClass.getDeclaredMethods())
            {
                if (RELEASE_INSTANCE_METHOD_NAME.equals(currentMethod.getName()) &&
                        currentMethod.getParameterTypes().length == 1 &&
                        currentMethod.getParameterTypes()[0].equals(ConstraintValidator.class))
                {
                    releaseInstanceMethod = currentMethod;
                    releaseInstanceMethodFound = true;
                    return;
                }

            }

            currentClass = currentClass.getSuperclass();
        }

        releaseInstanceMethodFound = false;
    }
}
