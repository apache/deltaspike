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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.destination.uc006;

import junit.framework.Assert;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigExtension;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigResolverProducer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ViewConfigBasePathValidationTest
{
    private ViewConfigExtension viewConfigExtension;

    private ViewConfigResolverProducer viewConfigResolverProducer;

    @Before
    public void before()
    {
        this.viewConfigExtension = new ViewConfigExtension();
        this.viewConfigResolverProducer = new ViewConfigResolverProducer(this.viewConfigExtension);
    }

    @After
    public void after()
    {
        this.viewConfigExtension.freeViewConfigCache(null);
    }

    @Test
    public void testInvalidBasePathUsage()
    {
        this.viewConfigExtension.addPageDefinition(PagesViolation.Wizard1.Start.class);

        try
        {
            this.viewConfigResolverProducer.createViewConfigResolver();
        }
        catch (IllegalStateException e)
        {
            if (!(e.getMessage().contains(View.class.getName()) &&
                e.getMessage().contains(Folder.class.getName())&&
                e.getMessage().contains("#basePath")))
            {
                Assert.fail("unexpected violation message found");
            }
            return;
        }
        Assert.fail("violation not found");
    }

    @Test
    public void testValidBasePathUsage()
    {
        this.viewConfigExtension.addPageDefinition(PagesViolation.Wizard2.Start.class);
        Assert.assertNotNull(this.viewConfigResolverProducer.createViewConfigResolver()); //won't cause an exception
    }
}
