package org.apache.deltaspike.test.jsf.impl.config.view.security;

import org.apache.deltaspike.test.jsf.impl.util.ArchiveUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

class DeploymentBuilder
{

    static WebArchive createDeployment(String warName)
    {
        return ShrinkWrap
                .create(WebArchive.class, warName)
                .addPackage(SecuredViewTest.class.getPackage())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(ArchiveUtils.getDeltaSpikeSecurityArchive())
                .addAsWebInfResource("default/WEB-INF/web.xml", "web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebResource("navigation/pages/index.xhtml", "/pages/noSecurity.xhtml")
                .addAsWebResource("navigation/pages/index.xhtml", "/pages/alwaysSucceeds.xhtml")
                .addAsWebResource("navigation/pages/index.xhtml", "/pages/alwaysDenied.xhtml")
                .addAsWebResource("navigation/pages/index.xhtml", "/pages/deniedFolder/inheritsDeniedFromFolder.xhtml")
                .addAsWebResource("navigation/pages/index.xhtml", "/pages/deniedFolder/voterNotCalled.xhtml")
                .addAsWebResource("navigation/pages/index.xhtml", "/pages/successFolder/compositionWithFolder.xhtml")
                .addAsWebResource("navigation/pages/index.xhtml", "/pages/inheritsDeniedFromSuper.xhtml")
                .addAsWebResource("navigation/pages/index.xhtml", "/pages/overridesSuper.xhtml");
    }
    
}
