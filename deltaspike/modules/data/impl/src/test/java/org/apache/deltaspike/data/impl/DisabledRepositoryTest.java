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
package org.apache.deltaspike.data.impl;

import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;

import javax.inject.Inject;
import org.apache.deltaspike.data.test.domain.Simple;

import org.apache.deltaspike.data.test.service.DisabledRepository;
import org.apache.deltaspike.data.test.service.SimpleRepository;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class DisabledRepositoryTest
{

    @Deployment
    public static Archive<?> deployment()
    {
        WebArchive archive = initDeployment()
                .addPackage(Simple.class.getPackage())
                .addClasses(SimpleRepository.class,
                        RepositoryDeactivator.class,
                        DisabledRepository.class
                );
       
        archive.delete("WEB-INF/classes/META-INF/apache-deltaspike.properties");
        archive.addAsWebInfResource("disabled/META-INF/apache-deltaspike.properties",
                        "classes/META-INF/apache-deltaspike.properties");
        return archive;
    }

    @Inject
    private SimpleRepository simpleRepository;

    @Inject
    private DisabledRepository disabledRepository;

    @Test
    public void disabledSimpleRepository()
    {
        try
        {
            simpleRepository.findAll();
            Assert.fail("Should have been failed because SimpleRepository was disabled");
        }
        catch (RuntimeException e)
        {
            Assert.assertNotNull(e);
        }

    }

    @Test
    public void disabledRepository()
    {
        try
        {
            disabledRepository.findAll();
            Assert.fail("Should have been failed because DisabledRepository was disabled");
        }
        catch (RuntimeException e)
        {
            Assert.assertNotNull(e);
        }

    }

}
