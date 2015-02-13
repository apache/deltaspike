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
package org.apache.deltaspike.partialbean.spi;

import java.util.List;
import org.apache.deltaspike.core.spi.activation.Deactivatable;

/**
 * The PartialBeanProvider allows to register a partial bean in BeforeBeanDiscovery as a completely new AnnotatedType,
 * to enable interceptors on the provided partial beans.
 *
 * Partial beans which will be collected later in ProcessAnnotatedType can't be intercepted.
 *
 * If other/new partial beans will be found later via ProcessAnnotatedType, they will be merged with the early provided
 * partial beans from the PartialBeanProvider's.
 */
public interface PartialBeanProvider extends Deactivatable
{
    List<PartialBeanDescriptor> get();
}
