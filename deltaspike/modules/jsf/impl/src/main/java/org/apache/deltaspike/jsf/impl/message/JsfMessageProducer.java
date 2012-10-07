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

import org.apache.deltaspike.core.util.ReflectionUtils;
import org.apache.deltaspike.jsf.message.JsfMessage;

/**
 * Produces a dependent JsfMessage
 */
@ApplicationScoped
public class JsfMessageProducer
{
    @Produces
    @Dependent
    public JsfMessage<?> createJsfMessage(InjectionPoint injectionPoint)
    {
        return createJsfMessageFor(injectionPoint, ReflectionUtils.getRawType(injectionPoint.getType()));
    }

    private JsfMessage<?> createJsfMessageFor(InjectionPoint injectionPoint, Class<Object> rawType)
    {
        //X TODO check if the JsfMessage should get injected into a UIComponent and use #getClientId()

        return new DefaultJsfMessage(rawType, null);
    }
}
