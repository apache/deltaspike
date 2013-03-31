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
package org.apache.deltaspike.core.api.literal;

import org.apache.deltaspike.core.api.message.LocaleResolver;
import org.apache.deltaspike.core.api.message.MessageContextConfig;
import org.apache.deltaspike.core.api.message.MessageInterpolator;
import org.apache.deltaspike.core.api.message.MessageResolver;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal for {@link org.apache.deltaspike.core.api.message.MessageContextConfig}
 */
public class MessageContextConfigLiteral extends AnnotationLiteral<MessageContextConfig> implements MessageContextConfig
{
    private static final long serialVersionUID = -5888417869986174834L;

    private final Class<? extends MessageResolver> messageResolver;
    private final Class<? extends MessageInterpolator> messageInterpolator;
    private final Class<? extends LocaleResolver> localeResolver;
    private final String[] messageSource;

    public MessageContextConfigLiteral()
    {
        this(MessageResolver.class, MessageInterpolator.class, LocaleResolver.class, new String[0]);
    }

    public MessageContextConfigLiteral(Class<? extends MessageResolver> messageResolver,
                                       Class<? extends MessageInterpolator> messageInterpolator,
                                       Class<? extends LocaleResolver> localeResolver,
                                       String[] messageSource)
    {
        this.messageResolver = messageResolver;
        this.messageInterpolator = messageInterpolator;
        this.localeResolver = localeResolver;
        this.messageSource = messageSource;
    }

    @Override
    public String[] messageSource()
    {
        return messageSource;
    }

    @Override
    public Class<? extends MessageResolver> messageResolver()
    {
        return messageResolver;
    }

    @Override
    public Class<? extends MessageInterpolator> messageInterpolator()
    {
        return messageInterpolator;
    }

    @Override
    public Class<? extends LocaleResolver> localeResolver()
    {
        return localeResolver;
    }
}
