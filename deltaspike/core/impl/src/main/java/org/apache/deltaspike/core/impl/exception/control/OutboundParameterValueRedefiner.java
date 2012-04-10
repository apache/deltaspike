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

import org.apache.deltaspike.core.api.exception.control.CaughtException;
import org.apache.deltaspike.core.api.metadata.builder.ParameterValueRedefiner;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 * Redefiner allowing to inject a non contextual instance of {@link CaughtException} into the first parameter. This
 * class is immutable.
 */
public class OutboundParameterValueRedefiner implements ParameterValueRedefiner
{
    final private CaughtException<?> event;
    final private BeanManager bm;
    final private Bean<?> declaringBean;
    final private HandlerMethodImpl<?> handlerMethod;

    /**
     * Sole constructor.
     *
     * @param event   instance of CaughtException to inject.
     * @param manager active BeanManager
     * @param handler Handler method this redefiner is for
     */
    public OutboundParameterValueRedefiner(final CaughtException<?> event, final BeanManager manager,
                                           final HandlerMethodImpl<?> handler)
    {
        this.event = event;
        this.bm = manager;
        this.declaringBean = handler.getBean(manager);
        this.handlerMethod = handler;
    }

    /**
     * {@inheritDoc}
     */
    public Object redefineParameterValue(ParameterValue value)
    {
        CreationalContext<?> ctx = this.bm.createCreationalContext(this.declaringBean);

        try
        {
            if (value.getPosition() == this.handlerMethod.getHandlerParameter().getPosition())
            {
                return event;
            }
            return value.getDefaultValue(ctx);
        } finally
        {
            if (ctx != null)
            {
                ctx.release();
            }
        }
    }
}
