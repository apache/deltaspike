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

import javax.ejb.embeddable.EJBContainer;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.webbeans.config.WebBeansContext;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.ContextControl;

/**
 * OpenWebBeans specific implementation of {@link org.apache.deltaspike.cdise.api.CdiContainer}.
 */
@SuppressWarnings("UnusedDeclaration")
public class OpenEjbContainerControl implements CdiContainer
{
    private ContextControl ctxCtrl = null;
    private Bean<ContextControl> ctxCtrlBean = null;
    private CreationalContext<ContextControl> ctxCtrlCreationalContext = null;

    private EJBContainer openEjbContainer = null;
    
    @Inject
    private BeanManager beanManager;

    @Override
    public  BeanManager getBeanManager()
    {
        return beanManager;
    }

    @Override
    public synchronized void boot()
    {
        if (openEjbContainer == null)
        {
            // this immediately boots the container
            openEjbContainer = EJBContainer.createEJBContainer(getConfiguration());

            // this magic code performs injection
            try
            {
                openEjbContainer.getContext().bind("inject", this);
            }
            catch (NamingException e)
            {
                throw new RuntimeException("Could not perform OpenEJB injection", e);
            }

            if (beanManager == null)
            {
                // this happens if the OpenEJB injection didnt work
                beanManager = WebBeansContext.getInstance().getBeanManagerImpl();
            }
        }
    }

    protected Map<?,?> getConfiguration()
    {
        Map<String, String> config = new HashMap<String, String>();

        return config;
    }

    @Override
    public synchronized void shutdown()
    {
        if (ctxCtrl != null)
        {
            ctxCtrlBean.destroy(ctxCtrl, ctxCtrlCreationalContext);

        }

        if (openEjbContainer != null)
        {
            openEjbContainer.close();
            openEjbContainer = null;
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
            Set<Bean<?>> beans = getBeanManager().getBeans(ContextControl.class);
            ctxCtrlBean = (Bean<ContextControl>) getBeanManager().resolve(beans);
            ctxCtrlCreationalContext = getBeanManager().createCreationalContext(ctxCtrlBean);
            ctxCtrl = (ContextControl)
                    getBeanManager().getReference(ctxCtrlBean, ContextControl.class, ctxCtrlCreationalContext);
        }
        return ctxCtrl;
    }
}
