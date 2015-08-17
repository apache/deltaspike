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
package org.apache.deltaspike.core.impl.activation;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.spi.activation.ClassDeactivator;
import org.apache.deltaspike.core.spi.activation.Deactivatable;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a default implementation of ClassDeactivator which uses {@see ConfigSource} to resolve configuration options
 * for whether a class is active or not.
 *
 * By design, this is not a well performant implementation.  It is a useful utility to avoid implementing the interface
 * manually and may be an easy way to spin up test archives with some classes purposefully disabled.
 */
public class DefaultClassDeactivator implements ClassDeactivator
{
    public static final String KEY_PREFIX = "deactivate.";

    private static final Logger LOG = Logger.getLogger(DefaultClassDeactivator.class.getName());

    @Override
    public Boolean isActivated(Class<? extends Deactivatable> targetClass)
    {
        final String key = KEY_PREFIX + targetClass.getName();
        final String value = ConfigResolver.getPropertyValue(key);
        if (value == null)
        {
            return null;
        }
        else
        {
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.log(Level.FINE, "Deactivation setting for {0} found to be {1} based on configuration.",
                        new Object[]{key, value});
            }
            return !Boolean.valueOf(value);
        }
    }
}
