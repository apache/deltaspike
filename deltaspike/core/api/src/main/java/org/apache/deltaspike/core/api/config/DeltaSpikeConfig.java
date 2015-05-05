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
package org.apache.deltaspike.core.api.config;

import java.io.Serializable;

/**
 * Marker interface for all classes used for dynamic configuration of DeltaSpike itself. The term <i>Dynamic
 * configuration</i> refers to values which can be determined and changed during runtime and shouldn't be accessed
 * during container boot time.
 *
 * <p>
 * All DeltaSpike dynamic configuration objects implement this interface so they can be found more easily. There is no
 * other functionality implied with this interface.</p>
 *
 * <p>
 * DeltaSpike uses a <i>type-safe configuration</i> approach for most internal configuration. Instead of writing a
 * properties file or XML, you just implement one of the configuration interfaces which will then be picked up as a
 * CDI bean. If* there is already a default configuration for some functionality in DeltaSpike, you can use &#064;
 * Specializes or &#064;Alternative to change those.</p>
 *
 * <p>
 * See {@link org.apache.deltaspike.core.api.config.base.DeltaSpikeBaseConfig} for static DeltaSpike configuration
 * based on properties.</p>
 *
 */
public interface DeltaSpikeConfig extends Serializable
{
}
