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

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.impl.scope.DeltaSpikeContextExtension;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;

import javax.enterprise.inject.Typed;

//keep it public for supporting #{dsWindowContext.getCurrentWindowId()} in addition to
//#{dsWindowContext.currentWindowId}
@Typed()
public class InjectableWindowContext implements WindowContext
{
    private static final long serialVersionUID = -3606786361833889628L;

    private transient volatile WindowContext windowContext;

    InjectableWindowContext(WindowContext windowContext)
    {
        this.windowContext = windowContext;
    }

    private WindowContext getWindowContext()
    {
        if (this.windowContext == null)
        {
            this.windowContext =
                BeanProvider.getContextualReference(DeltaSpikeContextExtension.class).getWindowContext();
        }
        return this.windowContext;
    }

    @Override
    public String getCurrentWindowId()
    {
        return getWindowContext().getCurrentWindowId();
    }

    @Override
    public void activateWindow(String windowId)
    {
        getWindowContext().activateWindow(windowId);
    }

    @Override
    public boolean closeWindow(String windowId)
    {
        return getWindowContext().closeWindow(windowId);
    }
}
