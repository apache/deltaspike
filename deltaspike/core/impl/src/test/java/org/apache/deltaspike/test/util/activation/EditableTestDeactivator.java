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

package org.apache.deltaspike.test.util.activation;

import org.apache.deltaspike.core.spi.activation.ClassDeactivator;
import org.apache.deltaspike.core.spi.activation.Deactivatable;

import java.util.HashMap;
import java.util.Map;

public class EditableTestDeactivator implements ClassDeactivator
{
    private static Map<Class<? extends Deactivatable>, Boolean> result = new HashMap<Class<? extends Deactivatable>, Boolean>();

    @Override
    public Boolean isActivated(Class<? extends Deactivatable> targetClass)
    {
        return result.get(targetClass);
    }

    public static void activate(Class<? extends Deactivatable> classToActivate)
    {
        result.put(classToActivate, true);
    }

    public static void deactivate(Class<? extends Deactivatable> classToActivate)
    {
        result.put(classToActivate, false);
    }
}
