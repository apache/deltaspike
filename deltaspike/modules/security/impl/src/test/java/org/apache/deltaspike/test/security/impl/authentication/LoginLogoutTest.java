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
package org.apache.deltaspike.test.security.impl.authentication;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.impl.exclude.extension.ExcludeExtension;
import org.apache.deltaspike.security.api.Identity;
import org.apache.deltaspike.security.api.authentication.UnexpectedCredentialException;
import org.apache.deltaspike.security.api.credential.LoginCredential;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;


/**
 * Test for {@link org.apache.deltaspike.security.api.authorization.annotation.Secured}
 */
@RunWith(Arquillian.class)
public class LoginLogoutTest
{
    @Inject
    private TestAuthenticator authenticator;

    @Inject
    private TestInquiryStorage testInquiryStorage;

    @Inject
    private ShopClient shopClient;

    @Inject
    private Identity identity;

    @Inject
    private FailedLoginFailedObserver failedLoginFailedObserver;

    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "loginLogoutTest.jar")
                .addPackage("org.apache.deltaspike.test.security.impl.authentication")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, "login-logout-test.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndSecurityArchive())
                .addAsLibraries(testJar)
                .addAsServiceProvider(Extension.class, ExcludeExtension.class)
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }

    @Test
    public void loginAndLogout()
    {
        final String userName = "spike";
        final String password = "apache";

        //init
        authenticator.register(userName, password);

        //start
        shopClient.login(userName, password);

        Assert.assertTrue(identity.isLoggedIn());
        Assert.assertEquals(userName, identity.getUser().getId());

        Assert.assertNotNull(shopClient.requestNewProduct("Security module for DeltaSpike"));


        shopClient.logout();
        Assert.assertFalse(identity.isLoggedIn());


        Assert.assertNotNull(shopClient.requestNewProduct("I18n module for DeltaSpike"));

        Assert.assertEquals(1, testInquiryStorage.getUserInquiries().size());
        Assert.assertEquals(userName, testInquiryStorage.getUserInquiries().iterator().next().getUserName());

        Assert.assertEquals(1, testInquiryStorage.getAnonymInquiries().size());

        Assert.assertFalse(testInquiryStorage.getUserInquiries().iterator().next().getInquiry()
                .equals(testInquiryStorage.getAnonymInquiries().iterator()));
    }

    @Test
    public void failedLogin()
    {
        final String userName = "spike";
        final String password = "apache";

        //init
        authenticator.register(userName, password);

        //start
        shopClient.login(userName, "123");

        Assert.assertFalse(identity.isLoggedIn());
    }

    //TODO use context-control
    @Test
    public void failedForcedReLogin()
    {
        final String userName = "spike";
        final String password = "apache";

        //init
        authenticator.register(userName, password);

        //start
        shopClient.login(userName, password);

        Assert.assertTrue(identity.isLoggedIn());
        Assert.assertEquals(userName, identity.getUser().getId());

        //X TODO stop and start new request via ContextControl - instead of:
        BeanProvider.getContextualReference(LoginCredential.class).invalidate();

        try
        {
            shopClient.login("xyz", "123");
        }
        catch (UnexpectedCredentialException e)
        {
            //noinspection ThrowableResultOfMethodCallIgnored
            Assert.assertTrue(failedLoginFailedObserver.getObservedException()
                    instanceof UnexpectedCredentialException);

            // still logged in
            Assert.assertTrue(identity.isLoggedIn());
            return;
        }

        Assert.fail();
    }

    //X TODO add tests for the events
}
