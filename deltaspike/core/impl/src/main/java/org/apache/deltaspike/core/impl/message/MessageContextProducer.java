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
package org.apache.deltaspike.core.impl.message;

import org.apache.deltaspike.core.api.config.annotation.DefaultConfiguration;
import org.apache.deltaspike.core.api.message.LocaleResolver;
import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.api.message.MessageInterpolator;
import org.apache.deltaspike.core.api.message.MessageResolver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

@ApplicationScoped
@SuppressWarnings("UnusedDeclaration")
public class MessageContextProducer
{
    @Inject
    @DefaultConfiguration
    private LocaleResolver localeResolver;

    @Inject
    @DefaultConfiguration
    private MessageInterpolator messageInterpolator;

    @Inject
    @DefaultConfiguration
    private MessageResolver messageResolver;

    @Produces
    @Typed(MessageContext.class)
    @Dependent
    protected MessageContext createDefaultMessageContext()
    {
        MessageContext.Config messageContextConfig = new DefaultMessageContext().config();

        messageContextConfig.change().messageInterpolator(messageInterpolator);
        messageContextConfig.change().localeResolver(localeResolver);
        messageContextConfig.change().messageResolver(messageResolver);

        return messageContextConfig.use().create();
    }
}
