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
package org.apache.deltaspike.core.api.provider;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.deltaspike.core.util.ClassUtils;


/**
 * <p>This class provides access to the {@link BeanManager}
 * by registering the current {@link BeanManager} in an extension and
 * making it available via a singleton factory for the current application.</p>
 * <p>This is really handy if you like to access CDI functionality
 * from places where no injection is available.</p>
 * <p>If a simple but manual bean-lookup is needed, it's easier to use the {@link BeanProvider}.</p>
 * <p/>
 * <p>As soon as an application shuts down, the reference to the {@link BeanManager} will be removed.<p>
 * <p/>
 * <p>Usage:<p/>
 * <pre>
 * BeanManager bm = BeanManagerProvider.getInstance().getBeanManager();
 * </pre>
 */
public class BeanManagerProvider implements Extension
{
    private static Boolean testMode;

    private static BeanManagerProvider bmp = null;

    private volatile Map<ClassLoader, BeanManagerHolder> bms = new ConcurrentHashMap<ClassLoader, BeanManagerHolder>();

    /**
     * Returns if the {@link BeanManagerProvider} has been initialized.
     * Usually it isn't needed to call this method in application code.
     * It's e.g. useful for other frameworks to check if DeltaSpike and the CDI container in general have been started.
     *
     * @return true if the bean-manager-provider is ready to be used
     */
    public static boolean isActive()
    {
        return bmp != null;
    }

    /**
     * Allows to get the current provider instance which provides access to the current {@link BeanManager}
     *
     * @throws IllegalStateException if the {@link BeanManagerProvider} isn't ready to be used.
     * That's the case if the environment isn't configured properly and therefore the {@link AfterBeanDiscovery}
     * hasn't be called before this method gets called.
     * @return the singleton BeanManagerProvider
     */
    public static BeanManagerProvider getInstance()
    {
        if (bmp == null)
        {
            //X TODO Java-EE5 support needs to be discussed
            // workaround for some Java-EE5 environments in combination with a special
            // StartupBroadcaster for bootstrapping CDI

            // CodiStartupBroadcaster.broadcastStartup();
            // here bmp might not be null (depends on the broadcasters)
        }
        if (bmp == null)
        {
            throw new IllegalStateException("No " + BeanManagerProvider.class.getName() + " in place! " +
                    "Please ensure that you configured the CDI implementation of your choice properly. " +
                    "If your setup is correct, please clear all caches and compiled artifacts.");
        }
        return bmp;
    }


    /**
     * The active {@link BeanManager} for the current application (/{@link ClassLoader}). This method will throw an
     * {@link IllegalStateException} if the BeanManager cannot be found.
     *
     * @return the current bean-manager, never <code>null</code>
     * @throws IllegalStateException if the BeanManager cannot be found
     */
    public BeanManager getBeanManager()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);

        BeanManagerHolder resultHolder = bms.get(classLoader);
        BeanManager result;

        if (resultHolder == null)
        {
            result = resolveBeanManagerViaJndi();

            if (result != null)
            {
                bms.put(classLoader, new RootBeanManagerHolder(result));
            }
        }
        else
        {
            result = resultHolder.getBeanManager();

            if (!(resultHolder instanceof RootBeanManagerHolder))
            {
                BeanManager jndiBeanManager = resolveBeanManagerViaJndi();

                if (jndiBeanManager != null && /*same instance check:*/jndiBeanManager != result)
                {
                    setRootBeanManager(jndiBeanManager);

                    result = jndiBeanManager;
                }
                else
                {
                    setRootBeanManager(result);
                }
            }
        }

        if (result == null)
        {
            throw new IllegalStateException("Unable to find BeanManager. " +
                    "Please ensure that you configured the CDI implementation of your choice properly.");
        }

        return result;
    }

    /**
     * It basically doesn't matter which of the system events we use,
     * but basically we use the {@link AfterBeanDiscovery} event since it allows to use the
     * {@link BeanManagerProvider} for all events which occur after the {@link AfterBeanDiscovery} event.
     *
     * @param afterBeanDiscovery event which we don't actually use ;)
     * @param beanManager        the BeanManager we store and make available.
     */
    protected void setBeanManager(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        setBeanManager(new BeanManagerHolder(beanManager));
    }

    public void setRootBeanManager(BeanManager beanManager)
    {
        setBeanManager(new RootBeanManagerHolder(beanManager));
    }

    private void setBeanManager(BeanManagerHolder beanManagerHolder)
    {
        BeanManagerProvider bmpFirst = setBeanManagerProvider(this);

        ClassLoader cl = ClassUtils.getClassLoader(null);

        if (beanManagerHolder instanceof RootBeanManagerHolder ||
                //the lat bm wins - as before, but don't replace a root-bmh with a normal bmh
                (!(bmpFirst.bms.get(cl) instanceof RootBeanManagerHolder)))
        {
            bmpFirst.bms.put(cl, beanManagerHolder);
        }

        //override in any case in test-mode
        /*
         * use:
         * new BeanManagerProvider() {
         * @Override
         * public void setTestMode() {
         *     super.setTestMode();
         *   }
         * }.setTestMode();
         *
         * to activate it
         */
        if (Boolean.TRUE.equals(testMode))
        {
            bmpFirst.bms.put(cl, beanManagerHolder);
        }

        //X TODO Java-EE5 support needs to be discussed
        //CodiStartupBroadcaster.broadcastStartup();
    }

    /**
     * Cleanup on container shutdown
     *
     * @param beforeShutdown cdi shutdown event
     */
    public void cleanupStoredBeanManagerOnShutdown(@Observes BeforeShutdown beforeShutdown)
    {
        bms.remove(ClassUtils.getClassLoader(null));
    }

    /**
     * Get the BeanManager from the JNDI registry.
     * <p/>
     * Workaround for JBossAS 6 (see EXTCDI-74)
     * {@link #setBeanManager(javax.enterprise.inject.spi.AfterBeanDiscovery, javax.enterprise.inject.spi.BeanManager)}
     * is called in context of a different {@link ClassLoader}
     *
     * @return current {@link javax.enterprise.inject.spi.BeanManager} which is provided via JNDI
     */
    private BeanManager resolveBeanManagerViaJndi()
    {
        try
        {
            return (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
        }
        catch (NamingException e)
        {
            //workaround didn't work -> force NPE
            return null;
        }
    }

    /**
     * This function exists to prevent findbugs to complain about
     * setting a static member from a non-static function.
     *
     * @param beanManagerProvider the bean-manager-provider which should be used if there isn't an existing provider
     * @return the first BeanManagerProvider
     */
    private static BeanManagerProvider setBeanManagerProvider(BeanManagerProvider beanManagerProvider)
    {
        if (bmp == null)
        {
            bmp = beanManagerProvider;
        }

        return bmp;
    }

    protected void setTestMode()
    {
        activateTestMode();
    }

    private static void activateTestMode()
    {
        testMode = true;
    }
}
