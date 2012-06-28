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
package org.apache.deltaspike.test.core.api.message.locale;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.Locale;

import org.apache.deltaspike.core.api.message.LocaleResolver;

/**
 * This alternative LocaleResolver replaces the DefaultLocaleResolver.
 * It allows us to set the Locale to different ones to test
 * variations. This is needed to avoid having test which by accident
 * only run in a single timezone/locale.
 */
@ApplicationScoped
@Alternative
public class ConfigurableLocaleResolver implements LocaleResolver
{
    private static final long serialVersionUID = 1927000487639667775L;
    private Locale locale;

    @PostConstruct
    public void init()
    {
        locale = Locale.getDefault();
    }

    @Override
    public Locale getLocale()
    {
        return locale;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }
}
