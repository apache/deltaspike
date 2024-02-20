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

import org.apache.deltaspike.data.test.TestTransactionStrategy;
import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.AuditedEntity;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;
import java.net.URL;

public abstract class TestDeployments {

    /**
     * Create a basic deployment with dependencies, beans.xml and persistence descriptor.
     *
     * @return Basic web archive.
     */
    public static WebArchive initDeployment()
    {
        return initDeployment(true);
    }

    public static WebArchive initDeployment(boolean addDefaultEntityManagerProducer)
    {
        return initDeployment(addDefaultEntityManagerProducer, EmptyAsset.INSTANCE);
    }
    
    public static WebArchive initDeployment(boolean addDefaultEntityManagerProducer, Asset beansXmlAsset)
    {
        Logging.reconfigure();

        WebArchive archive = ShrinkWrap
                .create(WebArchive.class, "test.war")
                // used by many tests, shouldn't interfere with others
                .addClasses(TransactionalTestCase.class, TestData.class, TestTransactionStrategy.class)
                .addPackages(true, AuditedEntity.class.getPackage())
                .addAsLibraries(getDeltaSpikeDataWithDependencies())
                .addAsWebInfResource("test-persistence.xml", "classes/META-INF/persistence.xml")
                .addAsWebInfResource(beansXmlAsset, "beans.xml");

        if (addDefaultEntityManagerProducer)
        {
            archive.addClass(EntityManagerProducer.class);
        }
        
        return archive;
    }

    public static File[] getDeltaSpikeDataWithDependencies()
    {
        return Maven.resolver().loadPomFromFile("pom.xml").resolve(
                "org.apache.deltaspike.core:deltaspike-core-api",
                "org.apache.deltaspike.core:deltaspike-core-impl",
                "org.apache.deltaspike.modules:deltaspike-partial-bean-module-api",
                "org.apache.deltaspike.modules:deltaspike-partial-bean-module-impl",
                "org.apache.deltaspike.modules:deltaspike-jpa-module-api",
                "org.apache.deltaspike.modules:deltaspike-jpa-module-impl",
                "org.apache.deltaspike.modules:deltaspike-data-module-api",
                "org.apache.deltaspike.modules:deltaspike-data-module-impl")
                .withTransitivity()
                .asFile();
    }

    public static void addToEarManifestIfExists(EnterpriseArchive archive, String resource)
    {
        URL url = TestDeployment.class.getClassLoader().getResource(resource);
        if (url != null) {
            archive.addAsManifestResource(resource);
        }
    }


}
