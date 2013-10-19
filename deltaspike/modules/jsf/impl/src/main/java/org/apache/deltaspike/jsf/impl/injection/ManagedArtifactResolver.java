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
package org.apache.deltaspike.jsf.impl.injection;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;
import java.util.Set;

@Typed()
public abstract class ManagedArtifactResolver
{
    public static final String JAVAX_FACES_CONVERT_PACKAGE_NAME = "javax.faces.convert";
    public static final String JAVAX_FACES_VALIDATOR_PACKAGE_NAME = "javax.faces.validator";

    /**
     * Constructor which prevents the instantiation of this class
     */
    private ManagedArtifactResolver()
    {
    }

    public static Converter resolveManagedConverter(Class<? extends Converter> converterClass)
    {
        if (JAVAX_FACES_CONVERT_PACKAGE_NAME.equals(converterClass.getPackage().getName()))
        {
            return null;
        }

        return getContextualReference(BeanManagerProvider.getInstance().getBeanManager(), converterClass);
    }

    public static Validator resolveManagedValidator(Class<? extends Validator> validatorClass)
    {
        if (JAVAX_FACES_VALIDATOR_PACKAGE_NAME.equals(validatorClass.getPackage().getName()))
        {
            return null;
        }

        return getContextualReference(BeanManagerProvider.getInstance().getBeanManager(), validatorClass);
    }

    private static <T> T getContextualReference(BeanManager beanManager, Class<T> type)
    {
        Set<Bean<?>> beans = beanManager.getBeans(type);

        if (beans == null || beans.isEmpty())
        {
            return null;
        }

        Bean<?> bean = beanManager.resolve(beans);

        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        @SuppressWarnings({ "unchecked", "UnnecessaryLocalVariable" })
        T result = (T) beanManager.getReference(bean, type, creationalContext);

        if (bean.getScope().equals(Dependent.class))
        {
            AbstractBeanStorage beanStorage = BeanProvider.getContextualReference(RequestDependentBeanStorage.class);

            //noinspection unchecked
            beanStorage.add(new DependentBeanEntry(result, bean, creationalContext));
        }

        return result;
    }
}
