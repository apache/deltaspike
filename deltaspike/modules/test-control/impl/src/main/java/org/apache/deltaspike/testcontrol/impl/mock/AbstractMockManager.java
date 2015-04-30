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
package org.apache.deltaspike.testcontrol.impl.mock;

import org.apache.deltaspike.testcontrol.api.junit.TestBaseConfig;
import org.apache.deltaspike.testcontrol.api.mock.DynamicMockManager;
import org.apache.deltaspike.testcontrol.api.mock.TypedMock;

import javax.enterprise.inject.Typed;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMockManager implements DynamicMockManager
{
    private Map<BeanCacheKey, Object> registeredMocks = new HashMap<BeanCacheKey, Object>();

    @Override
    public void addMock(Object mockInstance, Annotation... qualifiers)
    {
        //check if this method gets used without changing the default-config
        if (!TestBaseConfig.MockIntegration.ALLOW_MOCKED_BEANS &&
            !TestBaseConfig.MockIntegration.ALLOW_MOCKED_PRODUCERS)
        {
            throw new IllegalStateException("The support for mocked CDI-Beans is disabled " +
                "due to a reduced portability across different CDI-implementations. " +
                "Please set '" + TestBaseConfig.MockIntegration.ALLOW_MOCKED_BEANS_KEY + "' and/or '" +
                TestBaseConfig.MockIntegration.ALLOW_MOCKED_PRODUCERS_KEY + "' to 'true' " +
                "(in 'META-INF/apache-deltaspike.properties') on your test-classpath.");
        }

        Class<?> mockClass = mockInstance.getClass();
        Class<?> beanClass = mockClass.getSuperclass();

        if (beanClass == null)
        {
            beanClass = mockClass;
        }
        if (Object.class.equals(beanClass))
        {
            throw new IllegalArgumentException(mockInstance.getClass().getName() +
                " isn't a supported approach for mocking -> please extend from the original class.");
        }

        TypedMock typedMock = mockClass.getAnnotation(TypedMock.class);

        if (typedMock == null)
        {
            typedMock = beanClass.getAnnotation(TypedMock.class);
        }

        Class[] specifiedTypes = null;

        if (typedMock != null)
        {
            specifiedTypes = typedMock.value();
        }
        else
        {
            Typed typed = mockClass.getAnnotation(Typed.class);

            if (typed == null || typed.value().length == 0)
            {
                typed = beanClass.getAnnotation(Typed.class);
            }

            if (typed != null && typed.value().length > 0)
            {
                specifiedTypes = typed.value();
            }
        }

        if (specifiedTypes != null)
        {
            for (Class typedClass : specifiedTypes)
            {
                this.registeredMocks.put(new BeanCacheKey(typedClass, qualifiers), mockInstance);
            }
        }
        else
        {
            this.registeredMocks.put(new BeanCacheKey(beanClass, qualifiers), mockInstance);
        }
    }

    @Override
    public <T> T getMock(Class<T> beanClass, Annotation... qualifiers)
    {
        return (T)this.registeredMocks.get(new BeanCacheKey(beanClass, qualifiers));
    }

    @Override
    public void reset()
    {
        this.registeredMocks.clear();
    }
}
