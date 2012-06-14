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
package org.apache.deltaspike.test.core.api.config.injectable.numberconfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer;

@ApplicationScoped
@SuppressWarnings("UnusedDeclaration")
public class NumberConfigPropertyProducer extends BaseConfigPropertyProducer
{
    @Produces
    @Dependent
    @NumberConfig(name = "unused")
    public Float produceNumberProperty(InjectionPoint injectionPoint) throws ParseException
    {
        // resolve the annotation
        NumberConfig metaData = getAnnotation(injectionPoint, NumberConfig.class);

        // get the configured value from the underlying configuration system
        String configuredValue = getPropertyValue(metaData.name(), metaData.defaultValue());
        if (configuredValue == null)
        {
            return null;
        }

        // format according to the given pattern
        DecimalFormat df = new DecimalFormat(metaData.pattern(), new DecimalFormatSymbols(Locale.US));
        return df.parse(configuredValue).floatValue();
    }
}
