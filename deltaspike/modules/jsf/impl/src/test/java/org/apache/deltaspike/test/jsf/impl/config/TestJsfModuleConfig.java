package org.apache.deltaspike.test.jsf.impl.config;

import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;

@Specializes
@ApplicationScoped
public class TestJsfModuleConfig extends JsfModuleConfig
{
    private static final long serialVersionUID = -7188892423502607762L;

    @Override
    public ClientWindowConfig.ClientWindowRenderMode getDefaultWindowMode()
    {
        //TODO check issue with LAZY
        return ClientWindowConfig.ClientWindowRenderMode.CLIENTWINDOW;
    }
}
