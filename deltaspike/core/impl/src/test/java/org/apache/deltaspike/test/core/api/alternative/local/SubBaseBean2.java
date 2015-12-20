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
package org.apache.deltaspike.test.core.api.alternative.local;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

/**
 * Alternative which isn't configured as global alternative.
 *
 * (A normal alternative usually it would be in a different BDA - here we have the same BDA but no config in
 * beans.xml which simulates the behaviour - compared to {@link org.apache.deltaspike.test.core.api.alternative.global.SubBaseBean1} which is also not configured
 * in the beans.xml, but as global alternative (via DeltaSpike). Since we don't test the CDI implementation itself,
 * it's ok to simulate it. Otherwise it will break with CDI 1.1 or at least with the default behaviour of OWB.)
 */
@Alternative
@Dependent
//Workaround until different config files for unit tests work correctly
public class SubBaseBean2 extends BaseBean2
{
}
