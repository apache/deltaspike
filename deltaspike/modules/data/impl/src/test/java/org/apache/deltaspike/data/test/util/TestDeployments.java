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
package org.apache.deltaspike.data.test.util;

import java.net.URL;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.EntityManagerConfig;
import org.apache.deltaspike.data.api.EntityManagerDelegate;
import org.apache.deltaspike.data.api.EntityManagerResolver;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.FirstResult;
import org.apache.deltaspike.data.api.MaxResults;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryInvocationException;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.SingleResultType;
import org.apache.deltaspike.data.api.audit.CreatedOn;
import org.apache.deltaspike.data.api.audit.CurrentUser;
import org.apache.deltaspike.data.api.audit.ModifiedBy;
import org.apache.deltaspike.data.api.audit.ModifiedOn;
import org.apache.deltaspike.data.api.criteria.Criteria;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;
import org.apache.deltaspike.data.api.criteria.QuerySelection;
import org.apache.deltaspike.data.api.mapping.MappingConfig;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.RepositoryDefinitionException;
import org.apache.deltaspike.data.impl.RepositoryExtension;
import org.apache.deltaspike.data.impl.audit.AuditEntityListener;
import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.criteria.QueryCriteria;
import org.apache.deltaspike.data.impl.handler.QueryHandler;
import org.apache.deltaspike.data.impl.meta.RepositoryComponents;
import org.apache.deltaspike.data.impl.meta.RequiresTransaction;
import org.apache.deltaspike.data.impl.param.Parameters;
import org.apache.deltaspike.data.impl.property.Property;
import org.apache.deltaspike.data.impl.tx.TransactionalQueryRunner;
import org.apache.deltaspike.data.impl.util.EntityUtils;
import org.apache.deltaspike.data.spi.DelegateQueryHandler;
import org.apache.deltaspike.data.spi.QueryInvocationContext;
import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.AuditedEntity;
import org.apache.deltaspike.jpa.impl.transaction.EnvironmentAwareTransactionStrategy;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.utils.CdiContainerUnderTest;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.jboss.shrinkwrap.impl.base.filter.ExcludeRegExpPaths;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public abstract class TestDeployments
{

    public static Filter<ArchivePath> TEST_FILTER = new ExcludeRegExpPaths(".*Test.*class");

    public static WebArchive initDeployment()
    {
        return initDeployment(".*test.*");
    }

    /**
     * Create a basic deployment containing API classes, the Extension class and test persistence / beans descriptor.
     *
     * @return Basic web archive.
     */
    public static WebArchive initDeployment(String testFilter)
    {
        Logging.reconfigure();
        String descriptor = Descriptors.create(BeansDescriptor.class)
                .addDefaultNamespaces()
                .createAlternatives()
                    .clazz(EnvironmentAwareTransactionStrategy.class.getName())
                    .up()
                .exportAsString();
        WebArchive archive = ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addAsLibrary(createApiArchive())
                .addClasses(WebProfileCategory.class, TransactionalTestCase.class, TestData.class)
                .addClasses(RepositoryExtension.class, RepositoryDefinitionException.class)
                .addPackages(true, TEST_FILTER, createImplPackages())
                .addPackages(true, AuditedEntity.class.getPackage())
                .addPackages(true, new ExcludeRegExpPaths(testFilter), TransactionalTestCase.class.getPackage())
                .addAsWebInfResource("test-persistence.xml",
                        ArchivePaths.create("classes/META-INF/persistence.xml"))
                .addAsWebInfResource("META-INF/services/javax.enterprise.inject.spi.Extension",
                        ArchivePaths.create("classes/META-INF/services/javax.enterprise.inject.spi.Extension"))
                .addAsWebInfResource(new StringAsset(descriptor), ArchivePaths.create("beans.xml"));
        return addDependencies(archive);
    }

    public static Archive<?> finalizeDeployment(Class<?> testClass, WebArchive archive)
    {
        if (CdiContainerUnderTest.is("wls-.*"))
        {
            archive.addClass(testClass); // see https://issues.jboss.org/browse/ARQ-659
            EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                    .addAsModule(archive);
            ear.addAsLibraries(Maven.resolver()
                    .resolve("hsqldb:hsqldb:1.8.0.10")
                    .withTransitivity()
                    .asFile());
            addToEarManifestIfExists(ear, "weblogic-application.xml");
            addToEarManifestIfExists(ear, "TestDS-jdbc.xml");
            return ear;
        }
        return archive;
    }

    public static Package[] createImplPackages()
    {
        return new Package[] {
                AuditEntityListener.class.getPackage(),
                QueryBuilder.class.getPackage(),
                QueryCriteria.class.getPackage(),
                QueryHandler.class.getPackage(),
                RepositoryComponents.class.getPackage(),
                Parameters.class.getPackage(),
                EntityUtils.class.getPackage(),
                Property.class.getPackage(),
                TransactionalQueryRunner.class.getPackage()
        };
    }

    public static Archive<?> createApiArchive()
    {
        return ShrinkWrap.create(JavaArchive.class, "archive.jar")
                .addClasses(AbstractEntityRepository.class, Repository.class, EntityRepository.class,
                        FirstResult.class, MaxResults.class, Modifying.class,
                        Query.class, QueryParam.class, QueryResult.class,
                        EntityManagerConfig.class, EntityManagerResolver.class, SingleResultType.class,
                        QueryInvocationException.class, EntityManagerDelegate.class)
                .addClasses(Criteria.class, QuerySelection.class, CriteriaSupport.class)
                .addClasses(CreatedOn.class, CurrentUser.class, ModifiedBy.class, ModifiedOn.class)
                .addClasses(MappingConfig.class, QueryInOutMapper.class)
                .addClasses(DelegateQueryHandler.class, QueryInvocationContext.class, RequiresTransaction.class);
    }

    public static WebArchive addDependencies(WebArchive archive)
    {
        WebArchive webArchive= archive.addAsLibraries(
                Maven.resolver().loadPomFromFile("pom.xml").resolve(
                        "org.apache.deltaspike.core:deltaspike-core-api",
                        "org.apache.deltaspike.core:deltaspike-core-impl",
                        "org.apache.deltaspike.modules:deltaspike-partial-bean-module-api",
                        "org.apache.deltaspike.modules:deltaspike-partial-bean-module-impl",
                        "org.apache.deltaspike.modules:deltaspike-jpa-module-api",
                        "org.apache.deltaspike.modules:deltaspike-jpa-module-impl")
                        .withTransitivity()
                        .asFile());
        if (CdiContainerUnderTest.is("owb-.*") ||
            CdiContainerUnderTest.is("tomee-.*"))
        {
            JavaArchive javassistJar = ShrinkWrap.create(JavaArchive.class, "dsjavassist.jar")
                    .addPackages(true, "javassist");
            if (!javassistJar.getContent().isEmpty())
            {
                webArchive.addAsLibrary(javassistJar);
            }
        }

        /*
         * We need to add Javassist in case of Glassfish for the partial beans to work correctly. But as Javassist is
         * not on the test classpath like for the OWB and TomEE profiles, it is added using the Shrinkwrap Maven
         * resolver.
         */
        if (CdiContainerUnderTest.is("glassfish-.*"))
        {
            webArchive.addAsLibraries(Maven.resolver()
                    .resolve("javassist:javassist:3.12.0.GA")
                    .withTransitivity()
                    .asFile());
        }

        return webArchive;
    }

    public static void addToEarManifestIfExists(EnterpriseArchive archive, String resource)
    {
        URL url = TestDeployment.class.getClassLoader().getResource(resource);
        if (url != null) {
            archive.addAsManifestResource(resource);
        }
    }


}
