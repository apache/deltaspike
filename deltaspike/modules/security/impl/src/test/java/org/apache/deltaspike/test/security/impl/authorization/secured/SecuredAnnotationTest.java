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
package org.apache.deltaspike.test.security.impl.authorization.secured;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test for {@link org.apache.deltaspike.security.api.authorization.Secured}
 */
public abstract class SecuredAnnotationTest
{
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

    @Test(expected = AccessDeniedException.class)
    public void simpleInterceptorTestDenied()
    {
        SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
        testBean.getBlockedResult();
    }

    @Test(expected = AccessDeniedException.class)
    public void simpleInterceptorTestParentDenied()
    {
        SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
        testBean.someBlockedMethodFromParent();
    }

    @Test
    public void interceptorTestWithStereotypeOk()
    {
        SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
        Assert.assertEquals("result", testBean.getResult());
    }

    @Test
    public void interceptorTestWithStereotypeParentOk()
    {
        SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
        Assert.assertEquals("allfine", testBean.someFineMethodFromParent());
    }

    @Test(expected = AccessDeniedException.class)
    public void interceptorTestWithStereotypeDenied()
    {
        SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
        testBean.getBlockedResult();
    }

    @Test(expected = AccessDeniedException.class)
    public void interceptorTestWithStereotypeParentDenied()
    {
        SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
        testBean.someBlockedMethodFromParent();
    }

    @Test
    public void interceptorTestWithStereotypeWithValue()
    {
        SecuredBean5 testBean = BeanProvider.getContextualReference(SecuredBean5.class, false);

        Assert.assertEquals("result", testBean.getResult());

        try
        {
            testBean.getBlockedResult();
            Assert.fail("AccessDeniedException expect, but was not thrown");
        }
        catch (AccessDeniedException e)
        {
            //expected exception
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception: " + e);
        }
    }

    @Test
    public void simpleInterceptorTestOnMethods()
    {
        SecuredBean3 testBean = BeanProvider.getContextualReference(SecuredBean3.class, false);

        Assert.assertEquals("result", testBean.getResult());

        try
        {
            testBean.getBlockedResult();
            Assert.fail("AccessDeniedException expect, but was not thrown");
        }
        catch (AccessDeniedException e)
        {
            //expected exception
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception: " + e);
        }
    }

    @Test
    public void invocationOfMultipleSecuredStereotypes()
    {
        SecuredBean4 testBean = BeanProvider.getContextualReference(SecuredBean4.class, false);

        TestAccessDecisionVoter1 voter1 = BeanProvider.getContextualReference(TestAccessDecisionVoter1.class, false);
        TestAccessDecisionVoter2 voter2 = BeanProvider.getContextualReference(TestAccessDecisionVoter2.class, false);

        Assert.assertFalse(voter1.isCalled());
        Assert.assertFalse(voter2.isCalled());

        Assert.assertEquals("result", testBean.getResult());

        Assert.assertTrue(voter1.isCalled());
        Assert.assertTrue(voter2.isCalled());
    }
}
