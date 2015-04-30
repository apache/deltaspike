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
package org.apache.deltaspike.test.core.api.config.propertyconfigsource;

import org.apache.deltaspike.core.api.config.PropertyFileConfig;
import org.apache.deltaspike.test.category.EnterpriseArchiveProfileCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(EnterpriseArchiveProfileCategory.class)
public class ConfigPropertyEARTest extends BaseTestConfigProperty
{
    
    @Deployment
    public static EnterpriseArchive deployEar()
    {
        JavaArchive ejbJar = ShrinkWrap
                .create(JavaArchive.class, "ejb-jar.jar")
                .addClasses(BaseTestConfigProperty.class, ConfigPropertyEARTest.class,
                        MyBean.class, MyCustomEarPropertyFileConfig.class)
                .addAsResource(CONFIG_FILE_NAME)
                .addAsServiceProvider(PropertyFileConfig.class, MyCustomEarPropertyFileConfig.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset("org.apache.deltaspike.ProjectStage = UnitTest"),
                        "apache-deltaspike.properties");

        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        EnterpriseArchive enterpriseArchive = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsModule(ejbJar)
                .addAsModule(war)
                .setApplicationXML("application.xml");

        return enterpriseArchive;
    }
    
    @Test
    public void testInjectConfig()
    {
        // TODO Auto-generated method stub
        super.testInjectConfig();
    }
}
