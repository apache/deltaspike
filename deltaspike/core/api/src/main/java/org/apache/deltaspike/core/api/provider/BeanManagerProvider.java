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
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.util.ClassUtils;


/**
 * This class provides access to the {@link BeanManager} by registering the current {@link BeanManager} in an extension
 * and making it available via a singleton factory for the current application.
 *
 * <p>This is really handy when you need to access CDI functionality from places where no injection is available.</p>
 *
 * <p>If a simple but manual bean lookup is needed, it's easier to use the {@link BeanProvider}.</p>
 * 
 * <p>As soon as an application shuts down, the reference to the {@link BeanManager} is removed.</p>
 * 
 * <p>
 * Usage:

 * <pre>
 * BeanManager bm = BeanManagerProvider.getInstance().getBeanManager();</pre>
 * </p>
 * <p>
 * <b>Attention:</b> This approach is intended for use in user code at runtime. If BeanManagerProvider is used during
 * Container boot (in an Extension), non-portable behaviour results. During bootstrapping, an Extension shall
 * &#064;Inject BeanManager to get access to the underlying BeanManager (see e.g. {@link #cleanupFinalBeanManagers}).
 * This is the only way to guarantee that the right BeanManager is obtained in more complex Container scenarios.</p>
 */
public class BeanManagerProvider implements Extension
{
    private static final Logger  LOG = Logger.getLogger(BeanManagerProvider.class.getName());

    //for CDI 1.1+ delegation
    private static final Method CDI_CURRENT_METHOD;
    private static final Method CDI_CURRENT_BEAN_MANAGER_METHOD;

    private static BeanManagerProvider bmpSingleton;

    static
    {
        Class cdiClass = ClassUtils.tryToLoadClassForName("javax.enterprise.inject.spi.CDI");

        Method resolvedCdiCurrentMethod = null;
        Method resolvedCdiBeanManagerMethod = null;
        //only init methods if a cdi 1.1+ container is available and the delegation-mode isn't deactivated.
        //deactivation is e.g. useful if owb is used in "parallel mode" in a weld-based server.
        if (cdiClass != null && !"false".equalsIgnoreCase(
            ConfigResolver.getPropertyValue("deltaspike.bean-manager.delegate_lookup", Boolean.TRUE.toString())))
        {

            try
            {
                resolvedCdiCurrentMethod = cdiClass.getDeclaredMethod("current");
                resolvedCdiBeanManagerMethod = cdiClass.getDeclaredMethod("getBeanManager");
            }
            catch (Exception e)
            {
                LOG.log(Level.SEVERE, "Couldn't get method from " + cdiClass.getName(), e);
            }
        }

        //null if no init happened e.g. due to CDI 1.0 or deactivated delegation-mode
        CDI_CURRENT_METHOD = resolvedCdiCurrentMethod;
        CDI_CURRENT_BEAN_MANAGER_METHOD = resolvedCdiBeanManagerMethod;
    }

    /**
     * This data container is used for storing the BeanManager for each web application. This is needed in EAR or other
     * multi-webapp scenarios when the DeltaSpike classes (jars) are provided in a shared ClassLoader.
     */
    private static class BeanManagerInfo
    {
        /**
         * The BeanManager picked up via Extension loading.
         */
        private BeanManager loadTimeBm;

        /**
         * The final BeanManager. After the container did finally boot, we first try to resolve them from JNDI, and only
         * if we don't find any BM there we take the ones picked up at startup.
         */
        private BeanManager finalBm;

        /**
         * Whether the CDI Application has finally booted. Please note that this is only a nearby value as there is no
         * reliable event for this status in EE6.
         */
        private boolean booted;
    }

    /**
     * The BeanManagerInfo for the current ClassLoader.
     * 
     * <p><b>Attention:</b> This instance must only be used through the {@link #bmpSingleton} singleton!</p>
     */
    private volatile Map<ClassLoader, BeanManagerInfo> bmInfos = new ConcurrentHashMap<ClassLoader, BeanManagerInfo>();

    /**
     * Indicates whether the {@link BeanManagerProvider} has been initialized. Usually it's not necessary to call this
     * method in application code. It's useful e.g. for other frameworks to check if DeltaSpike and the CDI container in
     * general have been started.
     *
     * @return true if the BeanManagerProvider is ready to be used
     */
    public static boolean isActive()
    {
        return bmpSingleton != null;
    }

    /**
     * Returns the current provider instance which provides access to the current {@link BeanManager}.
     *
     * @throws IllegalStateException if the {@link BeanManagerProvider} isn't ready to be used. That's the case if the
     *                               environment isn't configured properly and therefore the {@link AfterBeanDiscovery}
     *                               hasn't been called before this method gets called.
     * @return the singleton BeanManagerProvider
     */
    public static BeanManagerProvider getInstance()
    {
        /*X TODO Java-EE5 support needs to be discussed
        if (bmpSingleton == null)
        {
            // workaround for some Java-EE5 environments in combination with a special
            // StartupBroadcaster for bootstrapping CDI

            // CodiStartupBroadcaster.broadcastStartup();
            // here bmp might not be null (depends on the broadcasters)
        }
        */

        if (bmpSingleton == null)
        {
            throw new IllegalStateException("No " + BeanManagerProvider.class.getName() + " in place! " +
                    "Please ensure that you configured the CDI implementation of your choice properly. " +
                    "If your setup is correct, please clear all caches and compiled artifacts.");
        }
        return bmpSingleton;
    }

    /**
     * It doesn't really matter which of the system events is used to obtain the BeanManager, but
     * {@link AfterBeanDiscovery} has been chosen since it allows all events which occur after the
     * {@link AfterBeanDiscovery} to use the {@link BeanManagerProvider}.
     *
     * @param afterBeanDiscovery event which we don't actually use ;)
     * @param beanManager        the BeanManager we store and make available.
     */
    public void setBeanManager(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        setBeanManagerProvider(this);

        BeanManagerInfo bmi = getBeanManagerInfo(ClassUtils.getClassLoader(null));
        bmi.loadTimeBm = beanManager;
    }

    /**
     * The active {@link BeanManager} for the current application (current {@link ClassLoader}). This method will throw
     * an {@link IllegalStateException} if the BeanManager cannot be found.
     *
     * @return the current BeanManager, never <code>null</code>
     *
     * @throws IllegalStateException if the BeanManager cannot be found
     */
    public BeanManager getBeanManager()
    {
        BeanManagerInfo bmi = getBeanManagerInfo(ClassUtils.getClassLoader(null));

        // warn the user if he tries to use the BeanManager before container startup
        if (!bmi.booted)
        {
            if (!isParentBeanManagerBooted())
            {
                LOG.warning("When using the BeanManager to retrieve Beans before the Container is started," +
                        " non-portable behaviour results!");

                // reset the flag to only issue the warning once.
                // this is a workaround for some containers which mess up EAR handling.
                bmi.booted = true;
            }
        }

        BeanManager result = bmi.finalBm;

        if (result == null)
        {
            synchronized (bmi)
            {
                result = bmi.finalBm;
                if (result == null)
                {
                    // first we look for a BeanManager from JNDI
                    result = resolveBeanManagerViaJndi();

                    // needs to be here to get a better performance and
                    // even in ee7 containers we can't rely on that lookup as a primary strategy
                    // (esp. in case of EAR based applications)
                    if (result == null)
                    {
                        result = resolveBeanManagerViaStaticHelper();
                    }

                    if (result == null)
                    {
                        // if none found, we take the one we got from the Extension loading
                        result = bmi.loadTimeBm;
                    }

                    if (result == null)
                    {
                        throw new IllegalStateException("Unable to find BeanManager. " +
                                "Please ensure that you configured the CDI implementation of your choice properly.");
                    }

                    // store the resolved BeanManager in the result cache until #cleanupFinalBeanManagers gets called
                    // -> afterwards the next call of #getBeanManager will trigger the final lookup
                    bmi.finalBm = result;
                }
            }
        }

        return result;
    }

    /**
     * By cleaning the final BeanManager map after the deployment gets validated, premature loading of information from
     * JNDI is prevented in cases where the container might not be fully setup yet.
     *
     * This might happen if the BeanManagerProvider is used in an extension during CDI bootstrap. This should be
     * generally avoided. Instead, an injected BeanManager should be used in Extensions and propagated using setters.
     *
     * In EARs with multiple webapps, each WAR might get a different Extension. This depends on the container used.
     */
    public void cleanupFinalBeanManagers(@Observes AfterDeploymentValidation adv)
    {
        for (BeanManagerInfo bmi : bmpSingleton.bmInfos.values())
        {
            bmi.finalBm = null;
            bmi.booted = true;

            /*possible issue with >weld< based servers:
            if #getBeanManager gets called in a custom AfterDeploymentValidation observer >after< this observer,
            the wrong bean-manager might get stored (not deterministic due to the unspecified order of observers).
            finally a bean-manager for a single bda will be stored and returned (which isn't the bm exposed via jndi).*/
        }
    }

    /**
     * Cleanup on container shutdown.
     *
     * @param beforeShutdown CDI shutdown event
     */
    public void cleanupStoredBeanManagerOnShutdown(@Observes BeforeShutdown beforeShutdown)
    {
        if (bmpSingleton == null)
        {
            // this happens if there has been a failure at startup
            return;
        }

        bmpSingleton.bmInfos.remove(ClassUtils.getClassLoader(null));
    }

    /**
     * Get the BeanManager from the JNDI registry.
     *
     * @return current {@link BeanManager} which is provided via JNDI
     */
    private BeanManager resolveBeanManagerViaJndi()
    {
        try
        {
            // this location is specified in JSR-299 and must be
            // supported in all certified EE environments
            return (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
        }
        catch (NamingException e)
        {
            //workaround didn't work -> return null
            return null;
        }
    }

    private BeanManager resolveBeanManagerViaStaticHelper()
    {
        if (CDI_CURRENT_METHOD != null && CDI_CURRENT_BEAN_MANAGER_METHOD != null)
        {
            try
            {
                Object cdiCurrentObject = CDI_CURRENT_METHOD.invoke(null);
                return (BeanManager) CDI_CURRENT_BEAN_MANAGER_METHOD.invoke(cdiCurrentObject);
            }
            catch (Throwable t)
            {
                LOG.log(Level.FINEST, "failed to delegate bean-manager lookup -> fallback to default.", t);
            }
        }
        return null;
    }

    /**
     * Get or create the BeanManagerInfo for the given ClassLoader.
     */
    private BeanManagerInfo getBeanManagerInfo(ClassLoader cl)
    {
        BeanManagerInfo bmi = bmpSingleton.bmInfos.get(cl);

        if (bmi == null)
        {
            synchronized (this)
            {
                bmi = bmpSingleton.bmInfos.get(cl);
                if (bmi == null)
                {
                    bmi = new BeanManagerInfo();
                    bmpSingleton.bmInfos.put(cl, bmi);
                    if (cl.getParent() != null && !bmpSingleton.bmInfos.containsKey(cl.getParent()))
                    {
                        bmpSingleton.bmInfos.put(cl.getParent(), bmi);
                    }
                }
            }
        }

        return bmi;
    }

    /**
     * This function exists to prevent findbugs from complaining about setting a static member from a non-static
     * function.
     *
     * @param beanManagerProvider the bean-manager-provider which should be used if there isn't an existing provider
     *
     * @return the first BeanManagerProvider
     */
    private static BeanManagerProvider setBeanManagerProvider(BeanManagerProvider beanManagerProvider)
    {
        if (bmpSingleton == null)
        {
            bmpSingleton = beanManagerProvider;
        }

        return bmpSingleton;
    }

    /**
     * @return whether a BeanManagerInfo for a parent ClassLoader is available and has the booted flag set.
     */
    private boolean isParentBeanManagerBooted()
    {
        BeanManagerInfo parentBmi = getParentBeanManagerInfo(ClassUtils.getClassLoader(null));

        return parentBmi != null && parentBmi.booted;
    }

    /**
     * This method recurses into the parent ClassLoaders and checks whether a BeanManagerInfo for it exists.
     *
     * @return the BeanManagerInfo of the parent ClassLoader hierarchy if any exists, or <code>null</code> if there is
     *         no {@link BeanManagerInfo} for the ClassLoaders in the hierarchy.
     */
    private BeanManagerInfo getParentBeanManagerInfo(ClassLoader classLoader)
    {
        ClassLoader parentClassLoader = classLoader.getParent();
        if (parentClassLoader == null || parentClassLoader.equals(ClassLoader.getSystemClassLoader()))
        {
            return null;
        }

        BeanManagerInfo bmi = getBeanManagerInfo(parentClassLoader);
        if (bmi == null)
        {
            bmi = getParentBeanManagerInfo(parentClassLoader);
        }

        return bmi;
    }

}
