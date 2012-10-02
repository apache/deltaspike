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

package org.apache.deltaspike.core.impl.exception.control;

import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.util.metadata.builder.ParameterValueRedefiner;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

/**
 * Redefiner allowing to inject a non contextual instance of {@link DefaultExceptionEvent} into the first parameter.
 * This class is immutable.
 */
class OutboundParameterValueRedefiner implements ParameterValueRedefiner
{
    private final ExceptionEvent<?> event;
    private final Bean<?> declaringBean;
    private final HandlerMethodImpl<?> handlerMethod;

    /**
     * Sole constructor.
     *
     * @param event         instance of DefaultExceptionEvent to inject.
     * @param handlerMethod Handler method this redefiner is for
     */
    OutboundParameterValueRedefiner(final ExceptionEvent<?> event, final HandlerMethodImpl<?> handlerMethod)
    {
        this.event = event;
        declaringBean = handlerMethod.getDeclaringBean();
        this.handlerMethod = handlerMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object redefineParameterValue(ParameterValue value)
    {
        CreationalContext<?> ctx = BeanManagerProvider.getInstance().getBeanManager()
                .createCreationalContext(declaringBean);

        try
        {
            if (value.getPosition() == handlerMethod.getHandlerParameter().getPosition())
            {
                return event;
            }
            return value.getDefaultValue(ctx);
        }
        finally
        {
            if (ctx != null)
            {
                ctx.release();
            }
        }
    }
}
