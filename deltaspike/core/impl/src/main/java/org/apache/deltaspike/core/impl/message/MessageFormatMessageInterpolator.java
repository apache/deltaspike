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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.io.Serializable;
import java.util.Locale;
import java.text.MessageFormat;

import org.apache.deltaspike.core.api.message.MessageInterpolator;

/**
 * This is an Alternative implementation of a {@link MessageInterpolator} which
 * uses java.text.MessageFormat for formatting.
 *
 * Please note that for some EE containers you might need to add this &lt;alternative&gt>
 * to all JARs and classpath entries beanx.xml files.
 *
 * {@inheritDoc}
 */
@ApplicationScoped
@Alternative
public class MessageFormatMessageInterpolator implements MessageInterpolator, Serializable
{
    private static final long serialVersionUID = -8854087197813424812L;

    @Override
    public String interpolate(String messageTemplate, Serializable[] arguments, Locale locale)
    {
        MessageFormat messageFormat = new MessageFormat(messageTemplate, locale);
        return messageFormat.format(arguments);
    }
}
