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
package org.apache.deltaspike.test.security.impl.authorization.securityparameterbinding;

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
public class SecurityParameterBindingTest
{
    @Deployment
    public static WebArchive deploy()
    {
        return ShrinkWrap.create(WebArchive.class, "security-parameter-binding-test.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndSecurityArchive())
                .addPackage(SecurityParameterBindingTest.class.getPackage())
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }

    @Test
    public void simpleInterceptorThrowsExceptionWhenImproperlyAnnotated()
    {
        try
        {
            SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
            testBean.getResult(new MockObject2(false));
            Assert.fail("Expected exception, IllegalStateException was not thrown");
        }
        catch (AccessDeniedException e)
        {
            // expected exception
        }
    }

    @Test
    public void simpleInterceptorDeniesTest()
    {
        try
        {
            SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
            testBean.getResult(new MockObject(false));
            Assert.fail("AccessDeniedException expect, but was not thrown");
        }
        catch (AccessDeniedException e)
        {
            // expected
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception: " + e);
        }
    }

    @Test
    public void simpleInterceptorAllowsTest()
    {
        SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
        Assert.assertTrue(testBean.getResult(new MockObject(true)));
    }

    @Test
    public void simpleInterceptorIgnoresUnsecuredMethods()
    {
        SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
        Assert.assertTrue(testBean.getResult(new MockObject(true)));
    }

    @Test
    public void simpleInterceptorTestOnMethodsDenies()
    {
        try
        {
            SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
            testBean.getBlockedResult(new MockObject(false));
            Assert.fail("AccessDeniedException expect, but was not thrown");
        }
        catch (AccessDeniedException e)
        {
            // expected
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception: " + e);
        }
    }

    @Test
    public void simpleInterceptorTestOnMethodsAllows()
    {
        SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
        Assert.assertTrue(testBean.getBlockedResult(new MockObject(true)));
    }

    @Test
    public void afterInvocationAuthorizerCheckWithAllowedResult()
    {
        SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
        Assert.assertTrue(testBean.getResult(true).isValue());
    }

    @Test
    public void afterInvocationAuthorizerCheckWithDeniedResult()
    {
        try
        {
        	SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
        	testBean.getResult(false);
            Assert.fail("AccessDeniedException expect, but was not thrown");
        }
        catch (AccessDeniedException e)
        {
            // expected
        }
    }

    @Test
    public void afterInvocationAuthorizerWithoutReturnType()
    {
        MethodInvocationParameter parameter = new MethodInvocationParameter();
        try
        {
            SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
            testBean.securityCheckAfterMethodInvocation(parameter);
            Assert.fail("AccessDeniedException expect, but was not thrown");
        }
        catch (AccessDeniedException e)
        {
            Assert.assertTrue(parameter.isMethodInvoked());
        }
    }

    @Test
    public void afterInvocationAuthorizerWithVoidReturnType()
    {
        MethodInvocationParameter parameter = new MethodInvocationParameter();
        try
        {
            SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
            testBean.securityCheckAfterMethodInvocationWithVoidResult(parameter);
            Assert.fail("AccessDeniedException expect, but was not thrown");
        }
        catch (AccessDeniedException e)
        {
            Assert.assertTrue(parameter.isMethodInvoked());
        }
    }
}
