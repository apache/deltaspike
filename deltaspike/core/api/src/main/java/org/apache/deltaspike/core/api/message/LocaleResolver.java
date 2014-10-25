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
package org.apache.deltaspike.core.api.message;


import java.io.Serializable;
import java.util.Locale;

import org.apache.deltaspike.core.api.config.DeltaSpikeConfig;

/**
 * Provides the current {@link java.util.Locale}.
 *
 * <p>
 * DeltaSpike provides a default implementation which returns the current system Locale.</p>
 * <p>
 * An application can provide custom implementation as an &#064;Alternative. This could e.g. examine a JSF View or the
 * Locale of any currently logged in User.</p>
 */
public interface LocaleResolver extends Serializable, DeltaSpikeConfig
{
    /**
     * @return the current locale
     */
    Locale getLocale();
}
