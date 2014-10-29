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
package org.apache.deltaspike.cdise.openejb;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.core.LocalInitialContext;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.webbeans.config.WebBeansContext;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * OpenEJB specific implementation of {@link org.apache.deltaspike.cdise.api.CdiContainer}.
 */
@SuppressWarnings("UnusedDeclaration")
public class OpenEjbContainerControl implements CdiContainer
{
    private static final Logger LOG = Logger.getLogger(OpenEjbContainerControl.class.getName());

    // global container config
    private static final Properties PROPERTIES = new Properties();

    static
    {
        // global properties
        PROPERTIES.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        PROPERTIES.setProperty(LocalInitialContext.ON_CLOSE, LocalInitialContext.Close.DESTROY.name());
        try
        {
            OpenEjbContainerControl.class.getClassLoader().loadClass("org.apache.openejb.server.ServiceManager");
            PROPERTIES.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        }
        catch (final Exception e)
        {
            // ignored
        }
    }


    private ContextControl ctxCtrl = null;
    private Bean<ContextControl> ctxCtrlBean = null;
    private CreationalContext<ContextControl> ctxCtrlCreationalContext = null;

    private Context context = null;
    
    private BeanManager beanManager;

    @Override
    public  BeanManager getBeanManager()
    {
        return beanManager;
    }

    @Override
    public synchronized void boot()
    {
        boot(null);
    }

    @Override
    public synchronized void boot(Map<?, ?> properties)
    {
        if (context == null)
        {
            // this immediately boots the container
            final Properties p = new Properties();
            p.putAll(PROPERTIES);
            if (properties != null) // override with user config
            {
                p.putAll(properties);
            }

            try
            {
                context = new InitialContext(p);
            }
            catch (final NamingException e)
            {
                throw new RuntimeException(e);
            }

            beanManager = WebBeansContext.currentInstance().getBeanManagerImpl();
        }
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
        }

        if (context != null)
        {
            try
            {
                context.close();
            }
            catch (final NamingException e)
            {
                // no-op
            }
            context = null;
        }

        ctxCtrl = null;
        ctxCtrlBean = null;
        ctxCtrlCreationalContext = null;
        beanManager = null;
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

            ctxCtrlCreationalContext = getBeanManager()
                    .createCreationalContext(ctxCtrlBean);

            ctxCtrl = (ContextControl)
                    getBeanManager().getReference(ctxCtrlBean, ContextControl.class, ctxCtrlCreationalContext);
        }
        return ctxCtrl;
    }
}
