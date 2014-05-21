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
package org.apache.deltaspike.testcontrol.impl.request;

import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.apache.deltaspike.testcontrol.spi.ExternalContainer;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

/**
 * Needed to allow the manual usage of
 * ContextControl#stopContext(RequestScoped.class)
 * and
 * ContextControl#startContext(RequestScoped.class)
 * within a test-method.
 * That can be useful in combination with the integration of myfaces-test for page-bean tests.
 */
@Decorator
//don't use an abstract decorator to keep the compatibility with old version of owb/weld
public class ContextControlDecorator implements ContextControl
{
    @Inject
    @Delegate
    private ContextControl wrapped;

    @Override
    public void startContexts()
    {
        wrapped.startContexts();

        if (isManualScopeHandling())
        {
            for (ExternalContainer externalContainer : CdiTestRunner.getActiveExternalContainers())
            {
                externalContainer.startScope(Singleton.class);
                externalContainer.startScope(ApplicationScoped.class);
                externalContainer.startScope(RequestScoped.class);
                externalContainer.startScope(SessionScoped.class);
                externalContainer.startScope(ConversationScoped.class);
            }
        }
    }

    @Override
    public void stopContexts()
    {
        if (isManualScopeHandling())
        {
            for (ExternalContainer externalContainer : CdiTestRunner.getActiveExternalContainers())
            {
                externalContainer.stopScope(ConversationScoped.class);
                externalContainer.stopScope(SessionScoped.class);
                externalContainer.stopScope(RequestScoped.class);
                externalContainer.stopScope(ApplicationScoped.class);
                externalContainer.stopScope(Singleton.class);
            }
        }

        wrapped.stopContexts();
    }

    @Override
    public void startContext(Class<? extends Annotation> scopeClass)
    {
        wrapped.startContext(scopeClass);

        if (isManuallyHandledRequest(scopeClass))
        {
            for (ExternalContainer externalContainer : CdiTestRunner.getActiveExternalContainers())
            {
                externalContainer.startScope(scopeClass);
            }
        }
    }

    @Override
    public void stopContext(Class<? extends Annotation> scopeClass)
    {
        wrapped.stopContext(scopeClass);

        if (isManuallyHandledRequest(scopeClass))
        {
            for (ExternalContainer externalContainer : CdiTestRunner.getActiveExternalContainers())
            {
                externalContainer.stopScope(scopeClass);
            }
        }
    }

    private boolean isManuallyHandledRequest(Class<? extends Annotation> scopeClass)
    {
        return RequestScoped.class.equals(scopeClass) && isManualScopeHandling();
    }

    private boolean isManualScopeHandling()
    {
        return !Boolean.TRUE.equals(CdiTestRunner.isAutomaticScopeHandlingActive());
    }
}
