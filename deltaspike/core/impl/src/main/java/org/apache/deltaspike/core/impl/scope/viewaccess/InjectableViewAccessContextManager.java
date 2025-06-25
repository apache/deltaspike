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
package org.apache.deltaspike.core.impl.scope.viewaccess;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.impl.scope.DeltaSpikeContextExtension;
import org.apache.deltaspike.core.spi.scope.viewaccess.ViewAccessContextManager;

import jakarta.enterprise.inject.Vetoed;

@Vetoed
class InjectableViewAccessContextManager implements ViewAccessContextManager
{
    private transient volatile ViewAccessContextManager viewAccessContextManager;

    public InjectableViewAccessContextManager(ViewAccessContextManager viewAccessContextManager)
    {
        this.viewAccessContextManager = viewAccessContextManager;
    }

    private ViewAccessContextManager getConversationManager()
    {
        if (this.viewAccessContextManager == null)
        {
            this.viewAccessContextManager =
                BeanProvider.getContextualReference(DeltaSpikeContextExtension.class).getViewAccessScopedContext();
        }
        return this.viewAccessContextManager;
    }

    @Override
    public void close()
    {
        getConversationManager().close();
    }
}
