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

import javax.enterprise.inject.Typed;
import java.util.Locale;

@Typed()
class DefaultMessageContext implements MessageContext
{
    private static final long serialVersionUID = -110779217295211303L;

    private Config config = new DefaultMessageContextConfig();

    DefaultMessageContext()
    {
    }

    DefaultMessageContext(Config config)
    {
        this.config = config;
    }

    @Override
    public MessageBuilder message()
    {
        return new DefaultMessageBuilder(this);
    }

    @Override
    public Config config()
    {
        return config;
    }

    @Override
    public Locale getLocale()
    {
        return config().getLocaleResolver().getLocale();
    }

    /*
     * generated
     */

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DefaultMessageContext))
        {
            return false;
        }

        DefaultMessageContext that = (DefaultMessageContext) o;

        //noinspection RedundantIfStatement
        if (!config.equals(that.config))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return config.hashCode();
    }
}
