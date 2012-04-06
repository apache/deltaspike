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

import org.apache.deltaspike.core.api.message.MessageResolver;
import org.apache.deltaspike.core.util.PropertyFileUtils;

import javax.enterprise.inject.Typed;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Typed()
class DefaultMessageResolver implements MessageResolver
{
    private static final long serialVersionUID = 5834411208472341006L;

    private final ResourceBundle messageBundle;

    DefaultMessageResolver(String messageBundleName, Locale locale)
    {
        ResourceBundle resolvedBundle;
        try
        {
            resolvedBundle = PropertyFileUtils.getResourceBundle(messageBundleName, locale);
        }
        catch (MissingResourceException e)
        {
            //X TODO log it
            resolvedBundle = null;
        }

        this.messageBundle = resolvedBundle;
    }

    @Override
    public String getMessage(String messageDescriptor)
    {
        if (this.messageBundle != null && messageDescriptor != null &&
            messageDescriptor.startsWith("{") && messageDescriptor.endsWith("}"))
        {
            try
            {
                return this.messageBundle.getString(messageDescriptor.substring(1, messageDescriptor.length() - 1));
            }
            catch (MissingResourceException e)
            {
                return MISSING_RESOURCE_MARKER + messageDescriptor + MISSING_RESOURCE_MARKER;
            }
        }

        return messageDescriptor;
    }
}
