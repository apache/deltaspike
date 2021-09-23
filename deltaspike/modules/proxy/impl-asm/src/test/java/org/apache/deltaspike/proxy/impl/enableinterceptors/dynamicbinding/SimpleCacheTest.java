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
package org.apache.deltaspike.proxy.impl.enableinterceptors.dynamicbinding;

import java.util.List;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.apache.deltaspike.proxy.util.EnableInterceptorsInterceptor;
import org.apache.deltaspike.test.proxy.impl.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SimpleCacheTest
{    
    @Deployment
    public static WebArchive war()
    {
        String simpleName = SimpleCacheTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        // CDI 1.0/Weld 1.x needs EnableInterceptorsInterceptor
        BeansDescriptor beansWithEnablingInterceptor = Descriptors.create(BeansDescriptor.class);
        beansWithEnablingInterceptor.getOrCreateInterceptors().clazz(EnableInterceptorsInterceptor.class.getName());
        
        // war archive needs SimpleCacheInterceptor enabled
        BeansDescriptor beans = Descriptors.create(BeansDescriptor.class);
        beans.getOrCreateInterceptors().clazz(SimpleCacheInterceptor.class.getName());
          
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
                .addPackage(SimpleCacheTest.class.getPackage())
                .addAsManifestResource(new StringAsset(beansWithEnablingInterceptor.exportAsString()), "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndProxyArchive())
                .addAsLibraries(testJar)
                .addAsServiceProvider(Extension.class, SimpleCacheExtension.class)
                .addAsWebInfResource(new StringAsset(beans.exportAsString()), "beans.xml");
    }

    @Inject
    private MyRepository myRepository;
    
    @Test
    public void testCache() throws Exception
    {
        List<String> users = myRepository.getAllUsers();
        
        Assert.assertNotNull(users);
        Assert.assertEquals(3, users.size());
        
        
        Assert.assertSame(users, myRepository.getAllUsers());
    }

}
