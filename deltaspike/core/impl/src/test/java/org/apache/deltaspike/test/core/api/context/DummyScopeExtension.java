package org.apache.deltaspike.test.core.api.context;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

/**
 * Registers the {@link DummyContext}
 */
public class DummyScopeExtension implements Extension
{
    private DummyContext context;

    public void registerDummyContext(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        context = new DummyContext(beanManager, true);
        afterBeanDiscovery.addContext(context);
    }
}
