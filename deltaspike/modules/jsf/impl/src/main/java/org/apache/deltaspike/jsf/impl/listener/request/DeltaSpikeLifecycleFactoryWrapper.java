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
package org.apache.deltaspike.jsf.impl.listener.request;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import java.util.Iterator;

public class DeltaSpikeLifecycleFactoryWrapper extends LifecycleFactory implements Deactivatable
{
    private final LifecycleFactory wrapped;

    private final boolean deactivated;

    /**
     * Constructor for wrapping the given {@link LifecycleFactory}
     *
     * @param wrapped lifecycle-factory which should be wrapped
     */
    public DeltaSpikeLifecycleFactoryWrapper(LifecycleFactory wrapped)
    {
        this.wrapped = wrapped;
        this.deactivated = !ClassDeactivationUtils.isActivated(getClass());
    }

    @Override
    public void addLifecycle(String s, Lifecycle lifecycle)
    {
        wrapped.addLifecycle(s, lifecycle);
    }

    @Override
    public Lifecycle getLifecycle(String s)
    {
        Lifecycle result = this.wrapped.getLifecycle(s);

        if (this.deactivated)
        {
            return result;
        }

        return new DeltaSpikeLifecycleWrapper(result);
    }

    @Override
    public Iterator<String> getLifecycleIds()
    {
        return wrapped.getLifecycleIds();
    }

    @Override
    public LifecycleFactory getWrapped()
    {
        return wrapped;
    }
}
