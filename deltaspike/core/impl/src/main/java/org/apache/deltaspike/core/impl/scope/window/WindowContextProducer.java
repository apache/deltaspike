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
package org.apache.deltaspike.core.impl.scope.window;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.impl.scope.DeltaSpikeContextExtension;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;

/**
 * This producer provides access to the internally created
 * {@link WindowContext} implementation.
 * It simply wraps through to the instance used in the
 * {@link org.apache.deltaspike.core.impl.scope.DeltaSpikeContextExtension}.
 */
@ApplicationScoped
public class WindowContextProducer
{
    @Inject
    private DeltaSpikeContextExtension deltaSpikeContextExtension;

    @Produces
    @Named("dsWindowContext")
    @Dependent
    public WindowContext getWindowContext()
    {
        return new InjectableWindowContext(deltaSpikeContextExtension.getWindowContext());
    }
}
