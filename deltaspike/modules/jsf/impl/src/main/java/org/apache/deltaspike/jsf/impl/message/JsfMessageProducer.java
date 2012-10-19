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
package org.apache.deltaspike.jsf.impl.message;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.deltaspike.jsf.api.message.JsfMessage;

/**
 * Produces a dependent JsfMessage
 */
@ApplicationScoped
public class JsfMessageProducer
{
    @Produces
    @Dependent
    public JsfMessage createJsfMessage(InjectionPoint injectionPoint)
    {
        if (! (injectionPoint.getType() instanceof ParameterizedType))
        {
            throw new IllegalArgumentException("JsfMessage must be used as generic type");
        }
        ParameterizedType paramType = (ParameterizedType) injectionPoint.getType();
        Type[] actualTypes = paramType.getActualTypeArguments();
        if (actualTypes.length != 1)
        {
            throw new IllegalArgumentException("JsfMessage must have the MessageBundle as generic type parameter");
        }

        return createJsfMessageFor(injectionPoint, actualTypes[0]);
    }

    private JsfMessage createJsfMessageFor(InjectionPoint injectionPoint, Type rawType)
    {
        //X TODO check if the JsfMessage should get injected into a UIComponent and use #getClientId()

        return new DefaultJsfMessage((Class) rawType, null);
    }
}
