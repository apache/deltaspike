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
package org.apache.deltaspike.cdise.weld;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

import org.apache.deltaspike.cdise.api.ContextControl;

/**
 * Weld specific impl of the {@link org.apache.deltaspike.cdise.api.ContextControl}
 */
@Dependent
@SuppressWarnings("UnusedDeclaration")
public class WeldContextControl implements ContextControl
{
    private ContextController contextController;

    @Inject
    private BeanManager beanManager;

    @Override
    public void startContexts()
    {
        startApplicationScope();
        startSessionScope();
        startRequestScope();
        startConversationScope();
    }

    @Override
    public void startContext(Class<? extends Annotation> scopeClass)
    {
        if (scopeClass.isAssignableFrom(ApplicationScoped.class))
        {
            startApplicationScope();
        }
        else if (scopeClass.isAssignableFrom(SessionScoped.class))
        {
            startSessionScope();
        }
        else if (scopeClass.isAssignableFrom(RequestScoped.class))
        {
            startRequestScope();
        }
        else if (scopeClass.isAssignableFrom(ConversationScoped.class))
        {
            startConversationScope();
        }
    }

    /**
     * Currently we can't stop the {@link ApplicationScoped} due to WELD-1072
     *
     * {@inheritDoc}
     */
    @Override
    public void stopContexts()
    {
        stopConversationScope();
        stopRequestScope();
        stopSessionScope();
        stopApplicationScope(); //can't be done because of WELD-1072
        stopSingletonScope();
    }

    @Override
    public void stopContext(Class<? extends Annotation> scopeClass)
    {
        if (scopeClass.isAssignableFrom(ApplicationScoped.class))
        {
            stopApplicationScope();
        }
        else if (scopeClass.isAssignableFrom(SessionScoped.class))
        {
            stopSessionScope();
        }
        else if (scopeClass.isAssignableFrom(RequestScoped.class))
        {
            stopRequestScope();
        }
        else if (scopeClass.isAssignableFrom(ConversationScoped.class))
        {
            stopConversationScope();
        }
        else if (scopeClass.isAssignableFrom(Singleton.class))
        {
            stopSingletonScope();
        }
    }

    /*
     * start scopes
     */
    private void startApplicationScope()
    {
        try
        {
            getContextController().startApplicationScope();
        }
        catch (IllegalStateException ise)
        {
            // weld throws an ISE if the context was already started...
        }
    }

    private void startSessionScope()
    {
        try
        {
            getContextController().startSessionScope();
        }
        catch (IllegalStateException ise)
        {
            // weld throws an ISE if the context was already started...
        }
    }

    private void startConversationScope()
    {
        try
        {
            getContextController().startConversationScope(null);
        }
        catch (IllegalStateException ise)
        {
            // weld throws an ISE if the context was already started...
        }
    }

    private void startRequestScope()
    {
        try
        {
            getContextController().startRequestScope();
        }
        catch (IllegalStateException ise)
        {
            // weld throws an ISE if the context was already started...
        }
    }

    /*
     * stop scopes
     */

    private void stopApplicationScope()
    {
        try
        {
            getContextController().stopApplicationScope();
        }
        catch (IllegalStateException ise)
        {
            // weld throws an ISE if the context was already stopped...
        }
    }

    private void stopSessionScope()
    {
        try
        {
            getContextController().stopSessionScope();
        }
        catch (IllegalStateException ise)
        {
            // weld throws an ISE if the context was already stopped...
        }
    }

    private void stopConversationScope()
    {
        try
        {
            getContextController().stopConversationScope();
        }
        catch (IllegalStateException ise)
        {
            // weld throws an ISE if the context was already stopped...
        }
    }

    private void stopRequestScope()
    {
        try
        {
            getContextController().stopRequestScope();
        }
        catch (IllegalStateException ise)
        {
            // weld throws an ISE if the context was already stopped...
        }
    }

    private void stopSingletonScope()
    {
        try
        {
            getContextController().stopSingletonScope();
        }
        catch (IllegalStateException ise)
        {
            // weld throws an ISE if the context was already stopped...
        }
    }

    private ContextController getContextController()
    {
        if (contextController != null)
        {
            return contextController;
        }

        contextController = new ContextController();
        return tryToInjectFields(contextController);
    }

    private <T> T tryToInjectFields(T instance)
    {
        CreationalContext creationalContext = beanManager.createCreationalContext(null);

        AnnotatedType annotatedType = beanManager.createAnnotatedType(instance.getClass());
        InjectionTarget injectionTarget = beanManager.createInjectionTarget(annotatedType);
        injectionTarget.inject(instance, creationalContext);
        return instance;
    }

}
