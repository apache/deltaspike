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
 * <p>Marker interface for all type-safe framework configs.</p>
 *
 * <p>All DeltaSpike Configuration objects implement this interface
 * so they can be found more easily. There is no other
 * functionality implied with this interface.</p>
 *
 * <p>DeltaSpike uses a <i>Typesafe Configuration</i> approach.
 * Instead of writing a properties file or XML, you just implement
 * one of the configuration interfaces which will then be picked up as
 * CDI bean. If there is already a default configuration for
 * some functionality in DeltaSpike, you can use &#064;Specializes or
 * &#064;Alternative to change those.</p>
 */
public interface DeltaSpikeConfig extends Serializable
{
}
