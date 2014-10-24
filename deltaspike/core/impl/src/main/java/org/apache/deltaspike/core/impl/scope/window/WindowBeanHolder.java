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

import org.apache.deltaspike.core.impl.scope.AbstractBeanHolder;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.spi.scope.window.WindowContextQuotaHandler;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.core.util.context.ContextualStorage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * This holder will store the window Ids and it's beans for the current
 * HTTP Session. We use standard SessionScoped bean to not need
 * to treat async-supported and similar headache.
 */
@SessionScoped
public class WindowBeanHolder extends AbstractBeanHolder<String>
{
    private static final long serialVersionUID = 6313493410718133308L;

    @Inject
    private WindowContextQuotaHandler windowContextQuotaHandler;

    private boolean windowContextQuotaHandlerEnabled;

    @PostConstruct
    protected void init()
    {
        Class<? extends Deactivatable> windowContextQuotaHandlerClass =
            ProxyUtils.getUnproxiedClass(windowContextQuotaHandler.getClass());

        this.windowContextQuotaHandlerEnabled = ClassDeactivationUtils.isActivated(windowContextQuotaHandlerClass);
    }

    @Override
    public ContextualStorage getContextualStorage(BeanManager beanManager, String key, boolean createIfNotExist)
    {
        ContextualStorage result = super.getContextualStorage(beanManager, key, createIfNotExist);
        if (this.windowContextQuotaHandlerEnabled)
        {
            //only check it once the storage was created successfully
            this.windowContextQuotaHandler.checkWindowContextQuota(key);
        }
        return result;
    }
}
