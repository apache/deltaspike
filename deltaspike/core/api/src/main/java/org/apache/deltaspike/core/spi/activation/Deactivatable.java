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
package org.apache.deltaspike.core.spi.activation;

/**
 * <p>Interface to allow easier detection of deactivatable classes.</p>
 *
 * <p>These classes are activated by default and can be disabled on demand (e.g. via CDI config).
 * Since CDI, JSF,... currently don't allow to deactivate default implementations,
 * DeltaSpike has to introduce a proprietary mechanism.</p>
 *
 * <p>This is e.g. used to disable CDI Extensions in DeltaSpike and might get
 * used for other Extension libraries as well.</p>
 *
 * <p><b>Note:</b> It is suggested that the implementations
 * use the {@link org.apache.deltaspike.core.util.ClassDeactivationUtils} for implementing the lookup</p>
 */
public interface Deactivatable
{
}
