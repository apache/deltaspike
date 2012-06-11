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

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.impl.exclude.extension.ExcludeExtension;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
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

/**
 * Test for {@link org.apache.deltaspike.security.api.authorization.annotation.Secured}
 */
@RunWith(Arquillian.class)
public class SecurityParameterBindingTest
{
   @Deployment
   public static WebArchive deploy()
   {
      JavaArchive testJar = ShrinkWrap
               .create(JavaArchive.class, SecurityParameterBindingTest.class.getSimpleName() + ".jar")
               .addPackage(SecurityParameterBindingTest.class.getPackage().getName())
               .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

      return ShrinkWrap.create(WebArchive.class)
               .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndSecurityArchive())
               .addAsLibraries(testJar)
               .addAsServiceProvider(Extension.class, ExcludeExtension.class)
               .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
   }

   @Test(expected = IllegalStateException.class)
   public void simpleInterceptorThrowsExceptionWhenImproperlyAnnotated()
   {
      SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
      testBean.getResult(new MockObject(true));
   }

   @Test(expected = AccessDeniedException.class)
   public void simpleInterceptorDeniesTest()
   {
      SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
      testBean.getBlockedResult(new MockObject(false));
   }

   @Test
   public void simpleInterceptorAllowsTest()
   {
      SecuredBean1 testBean = BeanProvider.getContextualReference(SecuredBean1.class, false);
      Assert.assertTrue(testBean.getBlockedResult(new MockObject(true)));
   }

   @Test
   public void simpleInterceptorIgnoresUnsecuredMethods()
   {
      SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
      Assert.assertTrue(testBean.getResult(new MockObject(true)));
   }

   @Test(expected = AccessDeniedException.class)
   public void simpleInterceptorTestOnMethodsDenies()
   {
      SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
      testBean.getBlockedResult(new MockObject(false));
   }

   @Test
   public void simpleInterceptorTestOnMethodsAllows()
   {
      SecuredBean2 testBean = BeanProvider.getContextualReference(SecuredBean2.class, false);
      Assert.assertTrue(testBean.getBlockedResult(new MockObject(true)));
   }
}
