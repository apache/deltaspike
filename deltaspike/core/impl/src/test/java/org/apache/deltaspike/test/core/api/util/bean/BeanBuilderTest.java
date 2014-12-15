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

package org.apache.deltaspike.test.core.api.util.bean;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ImmutableInjectionPoint;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@RunWith(Arquillian.class)
public class BeanBuilderTest
{
    @Deployment
    public static Archive<?> createTestArchive()
    {
        return ShrinkWrap
                .create(WebArchive.class, "beanBuilderTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(SimpleClass.class, WithInjectionPoint.class);
    }

    @Inject
    private BeanManager beanManager;

    @Test
    public void assertNonNullInjectionPointsFromBeanBuilder()
    {
        final BeanBuilder beanBuilder = new BeanBuilder(beanManager);
        final AnnotatedType<?> at = beanManager.createAnnotatedType(WithInjectionPoint.class);
        final Bean<?> newInjectionBean = beanBuilder.readFromType(at).create();

        for (final InjectionPoint ip : newInjectionBean.getInjectionPoints())
        {
            Assert.assertNotNull(ip);
        }
    }

    @Test
    public void assertNonNullInjectionPointsWhenOverriding()
    {
        final BeanBuilder beanBuilder = new BeanBuilder(beanManager);
        final AnnotatedType<?> at = beanManager.createAnnotatedType(WithInjectionPoint.class);
        beanBuilder.readFromType(at);

        // It's not easy to actually create these in the state we need, so we have to get the InjectionPonits,
        // create new ones with the correct info, null out the bean, set them as the new injection points
        // then move on.
        final Set<InjectionPoint> origInjectionPoints = beanBuilder.getInjectionPoints();
        beanBuilder.injectionPoints(beanBuilder.getInjectionPoints());

        final Set<InjectionPoint> newInjectionPoints = new HashSet<InjectionPoint>();
        for (InjectionPoint ip : origInjectionPoints)
        {
            newInjectionPoints.add(new ImmutableInjectionPoint((AnnotatedField) ip.getAnnotated(),
                    ip.getQualifiers(), null, ip.isTransient(), ip.isDelegate()));
        }
        beanBuilder.injectionPoints(newInjectionPoints);

        final Bean<?> newInjectionBean = beanBuilder.create();

        for (final InjectionPoint ip : newInjectionBean.getInjectionPoints())
        {
            Assert.assertNotNull(ip);
        }
    }
}
