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

import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.api.message.MessageResolver;
import org.apache.deltaspike.core.util.PropertyFileUtils;

import javax.enterprise.context.Dependent;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Dependent
class DefaultMessageResolver implements MessageResolver
{
    private static final long serialVersionUID = 5834411208472341006L;


    @Override
    public String getMessage(MessageContext messageContext, String messageTemplate)
    {
        // we can use {{ as escaping for now
        if (messageTemplate.startsWith("{{"))
        {
            // in which case we just cut of the first '{'
            return messageTemplate.substring(1);
        }

        if (messageTemplate.startsWith("{") && messageTemplate.endsWith("}"))
        {
            String resourceKey = messageTemplate.substring(1, messageTemplate.length() - 1);

            String bundleName = messageContext.getBundle();

            if (bundleName == null)
            {
                // using {} without a bundle is always an error
                return null;
            }

            try
            {
                Locale locale = messageContext.getLocale();
                ResourceBundle messageBundle = PropertyFileUtils.getResourceBundle(bundleName, locale);

                return messageBundle.getString(resourceKey);
            }
            catch (MissingResourceException e)
            {
                return null;
            }
        }

        return messageTemplate;
    }

}
