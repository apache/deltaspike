package org.apache.deltaspike.test.jsf.impl.config.view.security;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.security.api.authorization.Secured;

interface Pages extends ViewConfig
{

    class NoSecurity implements ViewConfig
    {
    }
    
    @Secured(AlwaysSucceedsVoter.class)
    class AlwaysSucceeds implements ViewConfig
    {
    }
    
    @Secured(AlwaysDeniedVoter.class)
    class AlwaysDenied implements ViewConfig
    {
    }
    
    @Secured(AlwaysDeniedVoter.class)
    interface DeniedFolder
    {
        
        class InheritsDeniedFromFolder implements ViewConfig
        {
        }
        
        @Secured(AlwaysSucceedsVoter.class)
        class VoterNotCalled implements ViewConfig
        {
        }
        
    }
    
    @Secured(AlwaysSucceedsVoter.class)
    interface SuccessFolder
    {
        
        @Secured(AlwaysDeniedVoter.class)
        class CompositionWithFolder implements ViewConfig
        {
        }
        
    }
    
    @Secured(AlwaysDeniedVoter.class)
    interface SuperConfig extends ViewConfig
    {
    }
    
    class InheritsDeniedFromSuper implements SuperConfig
    {
    }
    
    @Secured(AlwaysSucceedsVoter.class)
    class OverridesSuper implements SuperConfig
    {
    }
    
}
