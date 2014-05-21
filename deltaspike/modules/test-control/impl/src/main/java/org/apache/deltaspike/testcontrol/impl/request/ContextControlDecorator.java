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

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
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

    @Inject
    private Event<ManuallyHandledRequestEvent> manualRequestEvent;

    @Override
    public void startContexts()
    {
        wrapped.startContexts();
    }

    @Override
    public void stopContexts()
    {
        wrapped.stopContexts();
    }

    @Override
    public void startContext(Class<? extends Annotation> scopeClass)
    {
        wrapped.startContext(scopeClass);

        if (isManuallyHandledRequest(scopeClass))
        {
            manualRequestEvent.fire(new ManuallyHandledRequestEvent(ManuallyHandledRequestEvent.ManualAction.STARTED));
        }
    }

    @Override
    public void stopContext(Class<? extends Annotation> scopeClass)
    {
        wrapped.stopContext(scopeClass);

        if (isManuallyHandledRequest(scopeClass))
        {
            manualRequestEvent.fire(new ManuallyHandledRequestEvent(ManuallyHandledRequestEvent.ManualAction.STOPPED));
        }
    }

    private boolean isManuallyHandledRequest(Class<? extends Annotation> scopeClass)
    {
        return RequestScoped.class.equals(scopeClass) &&
            !Boolean.TRUE.equals(CdiTestRunner.isAutomaticScopeHandlingActive());
    }
}
