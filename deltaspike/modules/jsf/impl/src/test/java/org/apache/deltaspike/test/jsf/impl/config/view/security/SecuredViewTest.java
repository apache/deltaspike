package org.apache.deltaspike.test.jsf.impl.config.view.security;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import junit.framework.Assert;

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class SecuredViewTest
{

    @Drone
    private WebDriver driver;

    @ArquillianResource
    private URL contextPath;
    
    @Deployment(testable = false)
    public static WebArchive deploy()
    {
        return DeploymentBuilder.createDeployment("secured-view-test.war");
    }
    
    @Test
    public void testNoVoters() throws MalformedURLException
    {
        checkAccess("pages/noSecurity.xhtml", true);
    }
    
    @Test
    public void testVotersAllowAccess() throws MalformedURLException
    {
        checkAccess("pages/alwaysSucceeds.xhtml", true);
    }
    
    @Test
    public void testVotersDenyAccess() throws MalformedURLException
    {
        checkAccess("pages/alwaysDenied.xhtml", false);
    }
    
    private void checkAccess(String page, boolean expectSuccess) throws MalformedURLException
    {
        driver.get(new URL(contextPath, page).toString());
        Assert.assertEquals(expectSuccess, driver.findElements(By.id("indexPage")).size() > 0);
    }
    
}
