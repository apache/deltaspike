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
package org.apache.deltaspike.test.security.impl.authorization.securitybinding;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test for {@link org.apache.deltaspike.security.api.authorization.Secured}
 */
@RunWith(Arquillian.class)
public class SecurityBindingTest
{
    @Deployment
    public static WebArchive deploy()
    {
        return ShrinkWrap.create(WebArchive.class, "security-binding-test.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndSecurityArchive())
                .addPackage(SecurityBindingTest.class.getPackage())
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }

    @Test
    public void simpleInterceptorTestOk()
    {
        SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
        Assert.assertEquals("result", testBean.getResult());
    }

    @Test
    public void simpleInterceptorTestParentOk()
    {
        SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
        Assert.assertEquals("allfine", testBean.someFineMethodFromParent());
    }

    @Test
    public void simpleInterceptorTestDenied()
    {
        SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
        try {
            testBean.getBlockedResult();
            Assert.fail();
        } catch (AccessDeniedException e) {
            //expected
        }
    }

    @Test
    public void simpleInterceptorTestParentDenied()
    {
        SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
        try {
            testBean.someBlockedMethodFromParent();
            Assert.fail();
        } catch (AccessDeniedException e) {
            //expected
        }
    }


    @Test
    public void simpleInterceptorTestOnMethodsOk()
    {
        SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
        Assert.assertEquals("result", testBean.getResult());
    }

    @Test
    public void simpleInterceptorTestOnMethodsDenied()
    {
        SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
        try {
            testBean.getBlockedResult();
        } catch (AccessDeniedException e) {
            //expected
        }
    }
}
