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
package org.apache.deltaspike.core.api.activation;

import java.io.Serializable;
import java.util.Set;

/**
 * A class-deactivator allows to specify deactivated classes which can't be deactivated via std. CDI mechanisms.
 * A class-deactivator will be resolved from the environment via the default resolvers or via a custom resolver which
 * allows to use any type of configuration-format. An easy way to configure it permanently is e.g.
 * to use the service-loader approach. Furthermore, {@link AbstractClassDeactivator} is a convenience class which
 * allows an easier implementation. All classes which implement {@link Deactivatable} in-/directly, can be deactivated
 * with this mechanism. For all other classes/beans, you can use the veto mechanism provided by CDI.
 */
public interface ClassDeactivator extends Serializable
{
    /**
     * Provides classes which should be deactivated.
     *
     * @return classes which should be deactivated
     */
    Set<Class> getDeactivatedClasses();
}
