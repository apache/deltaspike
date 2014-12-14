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
package org.apache.deltaspike.test.api.config.typed;

import org.apache.deltaspike.core.api.config.base.TypedConfig;

import java.util.Date;

public interface TestConfig
{
    interface Valid
    {
        TypedConfig<String> STRING_VALUE =
            new TypedConfig<String>("deltaspike.test.string-value", "default");

        TypedConfig<Integer> INTEGER_VALUE =
            new TypedConfig<Integer>("deltaspike.test.integer-value", 14);

        TypedConfig<Float> FLOAT_VALUE =
            new TypedConfig<Float>("deltaspike.test.float-value", 1.1F);

        TypedConfig<Boolean> BOOLEAN_VALUE =
            new TypedConfig<Boolean>("deltaspike.test.boolean-value", Boolean.TRUE);

        TypedConfig<Class> CLASS_VALUE =
            new TypedConfig<Class>("deltaspike.test.class-value", TypedConfigTest.class);

        TypedConfig<Date> CUSTOM_TYPE_VALUE =
            new TypedConfig<Date>("deltaspike.test.date-value", new Date(1983, 4, 14), new DateConverter());

        TypedConfig<Integer> INTEGER_VALUE_NO_DEFAULT =
            new TypedConfig<Integer>("deltaspike.test.integer-no-default-value", null, Integer.class);
    }

    interface InvalidConfig
    {
        TypedConfig<Integer> CONFIG_WITHOUT_TYPE_INFORMATION =
            new TypedConfig<Integer>("deltaspike.test.invalid-config", null);
    }

    interface InvalidConfigValue
    {
        TypedConfig<Integer> WRONG_VALUE =
            new TypedConfig<Integer>("deltaspike.test.invalid-value", 10);
    }

    static class DateConverter
    {
        public Date convert(String value)
        {
            return new Date(new Long(value));
        }
    }
}
