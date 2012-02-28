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

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

/**
 * Weld specific implementation of {@link org.apache.deltaspike.cdise.api.CdiContainer}.
 */
@SuppressWarnings("UnusedDeclaration")
public class WeldContainerControl implements CdiContainer
{
    private Weld weld;
    private WeldContainer weldContainer;

    private ContextController contextController;

    @Override
    public BeanManager getBeanManager()
    {
        return this.weldContainer.getBeanManager();
    }

    @Override
    public void start()
    {
        bootContainer();
        startContexts();
    }

    @Override
    public void stop()
    {
        stopContexts();
        shutdownContainer();
    }

    @Override
    public void bootContainer()
    {
        this.weld = new Weld();
        this.weldContainer = weld.initialize();
    }

    @Override
    public void shutdownContainer()
    {
        this.weld.shutdown();
    }

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
        //stopApplicationScope(); //can't be done because of WELD-1072
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
        getContextController().startApplicationScope();
    }

    private void startSessionScope()
    {
        getContextController().startSessionScope();
    }

    private void startConversationScope()
    {
        getContextController().startConversationScope(null);
    }

    private void startRequestScope()
    {
        getContextController().startRequestScope();
    }

    /*
     * stop scopes
     */

    private void stopApplicationScope()
    {
        getContextController().stopApplicationScope();
    }

    private void stopSessionScope()
    {
        getContextController().stopSessionScope();
    }

    private void stopConversationScope()
    {
        getContextController().stopConversationScope();
    }

    private void stopRequestScope()
    {
        getContextController().stopRequestScope();
    }

    private void stopSingletonScope()
    {
        getContextController().stopSingletonScope();
    }

    private ContextController getContextController()
    {
        if (this.contextController != null)
        {
            return this.contextController;
        }

        this.contextController = new ContextController();
        return tryToInjectFields(this.contextController);
    }

    private <T> T tryToInjectFields(T instance)
    {
        BeanManager beanManager = getBeanManager();

        CreationalContext creationalContext = beanManager.createCreationalContext(null);

        AnnotatedType annotatedType = beanManager.createAnnotatedType(instance.getClass());
        InjectionTarget injectionTarget = beanManager.createInjectionTarget(annotatedType);
        injectionTarget.inject(instance, creationalContext);
        return instance;
    }
}
