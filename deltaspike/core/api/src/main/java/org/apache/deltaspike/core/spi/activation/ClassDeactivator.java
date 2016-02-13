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

import java.io.Serializable;

/**
 * <p>DeltaSpike allows you to deactivate pre-configured parts (like Extensions, event-broadcasters,...).
 * Therefore DeltaSpike offers {@link ClassDeactivator} and {@link Deactivatable}.</p>
 *
 * <p>A {@link ClassDeactivator} allows to specify deactivated classes (if they implement {@link Deactivatable})
 * which can't be deactivated/customized via std. CDI mechanisms
 * (like the veto-method or alternative/specialized CDI-beans).
 * This might be the case e.g. for CDI Extensions because CDI mechanisms are not available at startup time.</p>
 *
 * <p>Use it mainly to deactivate specific parts explicitly (blacklist approach),
 * if there is an issue with such parts (and waiting for the next release isn't an option).</p>
 *
 * <p>A class-deactivator will be resolved from the environment via the default resolvers or via a custom resolver which
 * allows to use any type of configuration-format. See {@link org.apache.deltaspike.core.api.config.ConfigResolver}
 * for more information about how to configure it. The configuration key is
 * <code>org.apache.deltaspike.core.spi.activation.ClassDeactivator</code></p>
 *
 * <p>All ClassDeactivators will get picked up in order of their ordinal and might explicitly activate or
 * deactivate {@link Deactivatable} classes. Returning a <code>null</code> value means that the ClassDeactivator
 * doesn't care about the Deactivatable class.</p>
 *
 * <p>An implementation has to be stateless.</p>
 */
public interface ClassDeactivator extends Serializable
{
    /**
     * Provides classes which should be deactivated.
     *
     * @param targetClass class which should be checked
     * @return {@link Boolean#FALSE} if class should get activated, {@link Boolean#FALSE} if class must be available
     *         and <code>null</code> to let it as is (defined by default or other
     */
    Boolean isActivated(Class<? extends Deactivatable> targetClass);
}
