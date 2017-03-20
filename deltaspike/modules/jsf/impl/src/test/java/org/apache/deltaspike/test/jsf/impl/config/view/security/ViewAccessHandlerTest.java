package org.apache.deltaspike.test.jsf.impl.config.view.security;

import javax.faces.component.UIViewRoot;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.jsf.api.security.ViewAccessHandler;
import org.apache.deltaspike.security.api.authorization.ErrorViewAwareAccessDeniedException;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class ViewAccessHandlerTest
{
    
    @Inject
    private ViewConfigResolver viewConfigResolver;
    @Inject
    private ViewAccessHandler viewAccessHandler;
    @Inject
    private AlwaysDeniedVoter deniedVoter;
    @Inject
    private AlwaysSucceedsVoter succeedsVoter;
    
    @Deployment
    public static WebArchive deploy()
    {
        return DeploymentBuilder.createDeployment("view-access-handler-test.war");
    }
    
    @Test
    public void testNoSecuredAnnotation()
    {
        testViewAccessHandlerMethods(Pages.NoSecurity.class, true, 0, 0);
    }
    
    @Test
    public void testSecuredAlwaysSucceeds()
    {
        testViewAccessHandlerMethods(Pages.AlwaysSucceeds.class, true, 0, 1);
    }
    
    @Test
    public void testSecuredAlwaysDenied()
    {
        testViewAccessHandlerMethods(Pages.AlwaysDenied.class, false, 1, 0);
    }
    
    @Test
    public void testInheritsDeniedFromFolder()
    {
        testViewAccessHandlerMethods(Pages.DeniedFolder.InheritsDeniedFromFolder.class, false, 1, 0);
    }
    
    @Test
    public void testCompositionWithDeniedFolder()
    {
        // voter on the view should not be called, since the voter on the folder will return a violation
        testViewAccessHandlerMethods(Pages.DeniedFolder.VoterNotCalled.class, false, 1, 0);
    }
    
    @Test
    public void testCompositionWithSuccessFolder()
    {
        // both voters should be called, since the folder will not return a violation
        testViewAccessHandlerMethods(Pages.SuccessFolder.CompositionWithFolder.class, false, 1, 1);
    }
    
    @Test
    public void testInheritsDeniedFromSuper()
    {
        testViewAccessHandlerMethods(Pages.InheritsDeniedFromSuper.class, false, 1, 0);
    }
    
    @Test
    public void testOverridesSuper()
    {
        testViewAccessHandlerMethods(Pages.OverridesSuper.class, true, 0, 1);
    }
    
    private void testViewAccessHandlerMethods(Class<? extends ViewConfig> page, boolean expectSuccess,
            int expectedDeniedInvocations, int expectedSuccessInvocations)
    {
        ViewConfigDescriptor descriptor = viewConfigResolver.getViewConfigDescriptor(page);
        String viewId = descriptor.getViewId();
        UIViewRoot viewRoot = new UIViewRoot();
        viewRoot.setViewId(viewId);
        
        Assert.assertEquals(expectSuccess, viewAccessHandler.canAccessView(page));
        checkInvocationCount(deniedVoter, expectedDeniedInvocations);
        checkInvocationCount(succeedsVoter, expectedSuccessInvocations);
        
        Assert.assertEquals(expectSuccess, viewAccessHandler.canAccessView(descriptor));
        checkInvocationCount(deniedVoter, expectedDeniedInvocations);
        checkInvocationCount(succeedsVoter, expectedSuccessInvocations);
        
        Assert.assertEquals(expectSuccess, viewAccessHandler.canAccessView(viewId));
        checkInvocationCount(deniedVoter, expectedDeniedInvocations);
        checkInvocationCount(succeedsVoter, expectedSuccessInvocations);
        
        Assert.assertEquals(expectSuccess, viewAccessHandler.canAccessView(viewRoot));
        checkInvocationCount(deniedVoter, expectedDeniedInvocations);
        checkInvocationCount(succeedsVoter, expectedSuccessInvocations);
        
        boolean actualSuccess;
        try
        {
            viewAccessHandler.checkAccessToView(page);
            actualSuccess = true;
        }
        catch (ErrorViewAwareAccessDeniedException e)
        {
            actualSuccess = false;
        }
        Assert.assertEquals(expectSuccess, actualSuccess);
        checkInvocationCount(deniedVoter, expectedDeniedInvocations);
        checkInvocationCount(succeedsVoter, expectedSuccessInvocations);
        
        try
        {
            viewAccessHandler.checkAccessToView(descriptor);
            actualSuccess = true;
        }
        catch (ErrorViewAwareAccessDeniedException e)
        {
            actualSuccess = false;
        }
        Assert.assertEquals(expectSuccess, actualSuccess);
        checkInvocationCount(deniedVoter, expectedDeniedInvocations);
        checkInvocationCount(succeedsVoter, expectedSuccessInvocations);
        
        try
        {
            viewAccessHandler.checkAccessToView(viewId);
            actualSuccess = true;
        }
        catch (ErrorViewAwareAccessDeniedException e)
        {
            actualSuccess = false;
        }
        Assert.assertEquals(expectSuccess, actualSuccess);
        checkInvocationCount(deniedVoter, expectedDeniedInvocations);
        checkInvocationCount(succeedsVoter, expectedSuccessInvocations);
        
        try
        {
            viewAccessHandler.checkAccessToView(viewRoot);
            actualSuccess = true;
        }
        catch (ErrorViewAwareAccessDeniedException e)
        {
            actualSuccess = false;
        }
        Assert.assertEquals(expectSuccess, actualSuccess);
        checkInvocationCount(deniedVoter, expectedDeniedInvocations);
        checkInvocationCount(succeedsVoter, expectedSuccessInvocations);
    }
    
    private void checkInvocationCount(TestVoter voter, int expectedInvocations)
    {
        Assert.assertEquals(expectedInvocations, voter.getInvocations());
        voter.reset();
    }
    
}
