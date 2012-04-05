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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Message
{

    /**
     * The default format string of this message.
     *
     * @return the format string
     */
    String value();

    /**
     * The format type of this method (defaults to {@link Format#PRINTF}).
     *
     * @return the format type
     */
    Format format() default Format.PRINTF;

    /**
     * The possible format types.
     */
    enum Format
    {

        /**
         * A {@link java.util.Formatter}-type format string.
         */
        PRINTF,
        /**
         * A {@link java.text.MessageFormat}-type format string.
         */
        MESSAGE_FORMAT,
        /**
         * An expression language type format string.
         */
        EXP_LANG
    }

}
