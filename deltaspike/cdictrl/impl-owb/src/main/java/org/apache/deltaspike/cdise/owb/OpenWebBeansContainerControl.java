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
package org.apache.deltaspike.cdise.owb;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContainerLifecycle;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * OpenWebBeans specific implementation of {@link org.apache.deltaspike.cdise.api.CdiContainer}.
 */
@SuppressWarnings("UnusedDeclaration")
public class OpenWebBeansContainerControl implements CdiContainer
{
    private static final Logger LOG = Logger.getLogger(OpenWebBeansContainerControl.class.getName());

    private ContainerLifecycle lifecycle;

    private ContextControl ctxCtrl = null;
    private Bean<ContextControl> ctxCtrlBean = null;
    private CreationalContext<ContextControl> ctxCtrlCreationalContext = null;

    @Override
    public BeanManager getBeanManager()
    {
        if (lifecycle == null)
        {
            return null;
        }
        return lifecycle.getBeanManager();
    }

    @Override
    public synchronized void boot()
    {
        lifecycle = WebBeansContext.currentInstance().getService(ContainerLifecycle.class);

        Object mockServletContextEvent = null;
        if (OpenWebBeansContextControl.isServletApiAvailable())
        {
            mockServletContextEvent = OwbHelper.getMockServletContextEvent();
        }

        lifecycle.startApplication(mockServletContextEvent);
    }

    @Override
    public void boot(Map<?, ?> properties)
    {
        // we do not yet support any configuration.
        boot();
    }

    @Override
    public synchronized void shutdown()
    {
        if (ctxCtrl != null)
        {
            try
            {
                ctxCtrl.stopContexts();
            }
            catch (Exception e)
            {
                // contexts likely already stopped
            }
            ctxCtrlBean.destroy(ctxCtrl, ctxCtrlCreationalContext);
            ctxCtrl = null;
        }

        if (lifecycle != null) 
        {
            Object mockServletContextEvent = null;
            if (OpenWebBeansContextControl.isServletApiAvailable())
            {
                mockServletContextEvent = OwbHelper.getMockServletContextEvent();
            }

            lifecycle.stopApplication(mockServletContextEvent);
        }
        lifecycle = null;
    }

    @Override
    public synchronized ContextControl getContextControl()
    {
        if (ctxCtrl == null)
        {
            BeanManager beanManager = getBeanManager();

            if (beanManager == null)
            {
                LOG.warning("If the CDI-container was started by the environment, you can't use this helper." +
                        "Instead you can resolve ContextControl manually " +
                        "(e.g. via BeanProvider.getContextualReference(ContextControl.class) ). " +
                        "If the container wasn't started already, you have to use CdiContainer#boot before.");

                return null;
            }
            Set<Bean<?>> beans = beanManager.getBeans(ContextControl.class);
            ctxCtrlBean = (Bean<ContextControl>) beanManager.resolve(beans);
            ctxCtrlCreationalContext = getBeanManager().createCreationalContext(ctxCtrlBean);
            ctxCtrl = (ContextControl)
                    getBeanManager().getReference(ctxCtrlBean, ContextControl.class, ctxCtrlCreationalContext);
        }
        return ctxCtrl;
    }

    @Override
    public String toString()
    {
        return "OpenWebBeansContainerControl";
    }
}
