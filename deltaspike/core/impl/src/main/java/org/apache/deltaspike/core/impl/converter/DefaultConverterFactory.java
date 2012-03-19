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
package org.apache.deltaspike.core.impl.converter;

import org.apache.deltaspike.core.api.converter.Converter;
import org.apache.deltaspike.core.api.converter.MetaDataAwareConverter;
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.spi.converter.ConverterFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class DefaultConverterFactory implements ConverterFactory
{
    //TODO discuss type logic, 1:n converters,...
    private Map<ConverterKey, Converter> converterMapping = new ConcurrentHashMap<ConverterKey, Converter>();

    @PostConstruct
    protected void initDefaultConverters()
    {
        addDefaultConverters();

        addCustomConverters();
    }

    @Override
    public <S, T> Converter<S, T> create(
        Class<S> sourceType, Class<T> targetType, Class<? extends Annotation> metaDataType)
    {
        //TODO throw an exception if there isn't the correct converter
        Converter<S, T> result;

        //try to find meta-data-aware converter
        result = this.converterMapping.get(new ConverterKey(sourceType, targetType, metaDataType));

        //for a simple custom configuration-annotation a custom converter isn't required
        //in this case the custom annotation is just used to keep the (string-based) property in a central place

        if (result != null)
        {
            return result;
        }

        return this.converterMapping.get(new ConverterKey(sourceType, targetType, null));
    }

    private void addDefaultConverters()
    {
        registerConverter(new StringToIntegerConverter());
        registerConverter(new StringToLongConverter());
    }

    private void addCustomConverters()
    {
        List<Converter> customConverters = getCustomConverters();

        for (Converter customConverter : customConverters)
        {
            registerConverter(customConverter);
        }
    }

    private List<Converter> getCustomConverters()
    {
        //TODO check OWB the following works with Weld, but not with OWB:
        //return BeanProvider.getContextualReferences(Converter.class, true, true);

        //workaround - un-cached lookup is ok, because it's done just once

        BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
        Set<Bean<?>> allBeans = beanManager.getBeans(Object.class, new AnyLiteral());

        List<Bean<Converter>> customConverterBeans = new ArrayList<Bean<Converter>>();

        for (Bean<?> currentBean : allBeans)
        {
            if (Converter.class.isAssignableFrom(currentBean.getBeanClass()) && isTypedAsConverter(currentBean))
            {
                customConverterBeans.add((Bean<Converter>) currentBean);
            }
        }

        List<Converter> converters = new ArrayList<Converter>(customConverterBeans.size());

        Converter currentConverter;
        for (Bean<Converter> currentBean : customConverterBeans)
        {
            currentConverter = (Converter) getContextualReference(currentBean.getBeanClass(), beanManager, currentBean);
            converters.add(currentConverter);
        }

        return converters;
    }

    private static <T> T getContextualReference(Class<T> type, BeanManager beanManager, Bean<?> foundBean)
    {
        Set<Bean<?>> beanSet = new HashSet<Bean<?>>(1);
        beanSet.add(foundBean);

        Bean<?> bean = beanManager.resolve(beanSet);

        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        @SuppressWarnings({ "unchecked", "UnnecessaryLocalVariable" })
        T result = (T) beanManager.getReference(bean, type, creationalContext);
        return result;
    }

    private boolean isTypedAsConverter(Bean<?> currentBean)
    {
        Set<Type> types = currentBean.getTypes();

        if (types == null)
        {
            return false;
        }

        for (Type currentType : types)
        {
            if (currentType instanceof Class && Converter.class.isAssignableFrom((Class) currentType))
            {
                return true;
            }
        }

        //can be the case e.g. with @Typed()
        return false;
    }

    protected void registerConverter(Converter converter)
    {
        Class sourceType = null;
        Class targetType = null;
        Class metaDataType = null;

        ParameterizedType parameterizedType;
        for (Type currentType : converter.getClass().getGenericInterfaces())
        {
            if (currentType instanceof ParameterizedType)
            {
                parameterizedType = (ParameterizedType) currentType;

                if (Converter.class.isAssignableFrom((Class<?>) parameterizedType.getRawType()))
                {
                    sourceType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    targetType = (Class<?>) parameterizedType.getActualTypeArguments()[1];

                    if (MetaDataAwareConverter.class.isAssignableFrom((Class<?>) parameterizedType.getRawType()))
                    {
                        metaDataType = (Class<?>) parameterizedType.getActualTypeArguments()[2];
                    }
                    break;
                }
            }
        }

        this.converterMapping.put(new ConverterKey(sourceType, targetType, metaDataType), converter);
    }
}
