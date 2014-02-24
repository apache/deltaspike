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

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;

@Typed()
abstract class MessageBundleContext
{
    private static final ThreadLocal<Bean> MESSAGE_BUNDLE_BEAN = new ThreadLocal<Bean>();

    private MessageBundleContext()
    {
        // prevent instantiation
    }

    static void setBean(Bean bean)
    {
        MESSAGE_BUNDLE_BEAN.set(bean);
    }

    static void reset()
    {
        MESSAGE_BUNDLE_BEAN.set(null);
        MESSAGE_BUNDLE_BEAN.remove();
    }

    static Bean getCurrentMessageBundleBean()
    {
        return MESSAGE_BUNDLE_BEAN.get();
    }
}
