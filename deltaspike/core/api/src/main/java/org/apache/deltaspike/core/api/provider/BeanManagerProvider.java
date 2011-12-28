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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.deltaspike.core.api.util.ClassUtils;


/**
 * <p>This class provides access to the BeanManager
 * by registring the current BeanManager in an extension and
 * making it available via a singleton factory</p>
 * <p>This is really handy if you like to access CDI functionality
 * from places where no injection is available.</p>
 *
 * <p>Usage:<p/>
 * <pre>
 * BeanManager bm = BeanManagerProvider.getInstance().getBeanManager();
 * </pre>
 */
public class BeanManagerProvider implements Extension
{
    private static BeanManagerProvider bmp = null;

    private volatile Map<ClassLoader, BeanManager> bms = new ConcurrentHashMap<ClassLoader, BeanManager>();

    /**
     * Returns if the {@link BeanManagerProvider} has been initialized
     * @return true if the bean-manager-provider is ready to be used
     */
    public static boolean isActive()
    {
        return bmp != null;
    }

    /**
     * Singleton accessor
     * @return the singleton BeanManagerProvider
     */
    public static BeanManagerProvider getInstance()
    {
        if(bmp == null)
        {
            //X TODO
            // workaround for Mojarra (in combination with OWB and a custom WebBeansConfigurationListener and a custom
            // StartupBroadcaster for bootstrapping CDI)
            // CodiStartupBroadcaster.broadcastStartup();
            // here bmp might not be null (depends on the broadcasters)
        }
        if(bmp == null)
        {
            throw new IllegalStateException("no " + BeanManagerProvider.class.getName() + " in place! " +
                "Please ensure that you configured the CDI implementation of your choice properly. " +
                "If your setup is correct, please clear all caches and compiled artifacts. " +
                "If there is still a problem, try one of the controlled bootstrapping add-ons for the CDI " +
                    "implementation you are using.");
        }
        return bmp;
    }


    /**
     * The active {@link BeanManager} for the current {@link ClassLoader}
     * @return the current bean-manager
     */
    public BeanManager getBeanManager()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);

        BeanManager result = bms.get(classLoader);

        if (result == null)
        {
            result = resolveBeanManagerViaJndi();

            if(result != null)
            {
                bms.put(classLoader, result);
            }
        }
        return result;
    }

    /**
     * <p></p>Get a Contextual Reference by it's type and annotation.
     * You can use this method to get contextual references of a given type.
     * A 'Contextual Reference' is a proxy which will automatically resolve
     * the correct contextual instance when you access any method.</p>
     *
     * <p><b>Attention:</b> You shall not use this method to manually resolve a
     * &#064;Dependent bean! The reason is that this contextual instances do usually
     * live in the well defined lifecycle of their injection point (the bean they got
     * injected into). But if we manually resolve a &#064;Dependent bean, then it does <b>not</b>
     * belong to such a well defined lifecycle (because &#064;Dependent it is not
     * &#064;NormalScoped) and thus will not automatically be
     * destroyed at the end of the lifecycle. You need to manually destroy this contextual instance via
     * {@link javax.enterprise.context.spi.Contextual#destroy(Object, javax.enterprise.context.spi.CreationalContext)}.
     * Thus you also need to manually store the CreationalContext and the Bean you
     * used to create the contextual instance which this method will not provide.</p>
     *
     * @param type the type of the bean in question
     * @param qualifiers additional qualifiers which further distinct the resolved bean
     * @param <T> target type
     * @return the resolved Contextual Reference
     */
    public <T> T getContextualReference(Class<T> type, Annotation... qualifiers)
    {
        BeanManager beanManager = getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(type, qualifiers);

        if (beans == null || beans.isEmpty())
        {
            throw new IllegalStateException("Could not find beans for Type=" + type
                                            + " and qualifiers:" + Arrays.toString(qualifiers));
        }

        return getReference(type, beanManager, beans);
    }

    /**
     * <p>Get a Contextual Reference by it's EL Name.
     * This only works for beans with the &#064;Named annotation.</p>
     *
     * <p><b>Attention:</b> please see the notes on manually resolving &#064;Dependent bean
     * in {@link #getContextualReference(Class, java.lang.annotation.Annotation...)}!</p>
     *
     *
     * @param type the type of the bean in question - only use Object.class if the type is unknown in dyn. use-cases
     * @param name the EL name of the bean
     * @param <T> target type
     * @return the resolved Contextual Reference
     */
    public <T> T getContextualReference(Class<T> type, String name)
    {
        BeanManager beanManager = getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(name);

        return getReference(type, beanManager, beans);
    }

    /**
     * Internal helper method to resolve the right bean and
     * resolve the contextual reference.
     * @param type the type of the bean in question
     * @param beanManager current bean-manager
     * @param beans beans in question
     * @param <T> target type
     * @return the contextual reference
     */
    private <T> T getReference(Class<T> type, BeanManager beanManager, Set<Bean<?>> beans)
    {
        Bean<?> bean = beanManager.resolve(beans);

        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        T result = (T)beanManager.getReference(bean, type, creationalContext);
        return result;
    }

    /**
     * Get the BeanManager from the JNDI registry.
     *
     * Workaround for jboss 6 (EXTCDI-74)
     * {@link #setBeanManager(javax.enterprise.inject.spi.AfterBeanDiscovery, javax.enterprise.inject.spi.BeanManager)}
     * is called in context of a different classloader
     *
     * @return current {@link javax.enterprise.inject.spi.BeanManager} which is provided via jndi
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
     * It basiscally doesn't matter which of the system events we use,
     * but basically we
     * @param afterBeanDiscovery event which we don't actually use ;)
     * @param beanManager the BeanManager we store and make available.
     */
    public void setBeanManager(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        BeanManagerProvider bmpFirst = setBeanManagerProvider(this);

        ClassLoader cl = ClassUtils.getClassLoader(null);
        bmpFirst.bms.put(cl, beanManager);

        //X TODO os890: CodiStartupBroadcaster.broadcastStartup();
    }

    /**
     * Cleanup on container shutdown
     * @param beforeShutdown cdi shutdown event
     */
    public void cleanupStoredBeanManagerOnShutdown(@Observes BeforeShutdown beforeShutdown)
    {
        bms.remove(ClassUtils.getClassLoader(null));
    }

    /**
     * This function exists to prevent findbugs to complain about
     * setting a static member from a non-static function.
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
}
