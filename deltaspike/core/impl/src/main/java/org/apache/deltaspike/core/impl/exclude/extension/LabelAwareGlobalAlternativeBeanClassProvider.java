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
package org.apache.deltaspike.core.impl.exclude.extension;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.spi.alternative.AlternativeBeanClassProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LabelAwareGlobalAlternativeBeanClassProvider implements AlternativeBeanClassProvider
{
    private static final String GLOBAL_ALTERNATIVES = "globalAlternatives.";
    private static final String LABELED_ALTERNATIVES = "labeledAlternatives";
    private static final String ACTIVE_ALTERNATIVE_LABEL_KEY = "activeAlternativeLabel";

    private static final Logger LOG = Logger.getLogger(LabelAwareGlobalAlternativeBeanClassProvider.class.getName());

    @Override
    public Map<String, String> getAlternativeMapping()
    {
        Map<String, String> result = new HashMap<String, String>();

        String alternativeLabel = ConfigResolver.getPropertyValue(ACTIVE_ALTERNATIVE_LABEL_KEY);

        String activeQualifierLabel = null;
        if (alternativeLabel != null)
        {
            activeQualifierLabel = LABELED_ALTERNATIVES + "[" + alternativeLabel + "].";
        }

        Map<String, String> allProperties = ConfigResolver.getAllProperties();
        for (Map.Entry<String, String> property : allProperties.entrySet())
        {
            if (activeQualifierLabel != null && property.getKey().startsWith(activeQualifierLabel))
            {
                String interfaceName = property.getKey().substring(activeQualifierLabel.length());
                String implementation = property.getValue();
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.fine("Enabling labeled alternative for interface " + interfaceName + ": " + implementation);
                }

                result.put(interfaceName, implementation);
            }
            else if (property.getKey().startsWith(GLOBAL_ALTERNATIVES))
            {
                String interfaceName = property.getKey().substring(GLOBAL_ALTERNATIVES.length());
                String implementation = property.getValue();
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.fine("Enabling global alternative for interface " + interfaceName + ": " + implementation);
                }

                if (!result.containsKey(interfaceName)) //don't override labeled alternatives
                {
                    result.put(interfaceName, implementation);
                }
            }
        }

        return result;
    }
}
