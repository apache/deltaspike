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
package org.apache.deltaspike.test.jsf.impl.config.view.validation;

import junit.framework.Assert;
import org.apache.deltaspike.core.api.config.view.metadata.ConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigExtension;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigPathValidator;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigResolverProducer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContextEvent;
import java.util.ArrayList;
import java.util.List;

public class ViewConfigPathValidatorTest
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
    public void testValidViewConfig()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Index.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        List<String> supportedExtensions = new ArrayList<String>();
        supportedExtensions.add(View.Extension.XHTML);

        try
        {
            new MockedViewConfigPathValidator(true).validateViewConfigPaths(null, viewConfigResolver, supportedExtensions);
        }
        catch (Exception e)
        {
            Assert.fail("valid view-config was reported as invalid");
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingPath()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Index.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        List<String> supportedExtensions = new ArrayList<String>();
        supportedExtensions.add(View.Extension.XHTML);
        new MockedViewConfigPathValidator(false).validateViewConfigPaths(null, viewConfigResolver, supportedExtensions);
    }

    @Test
    public void testMissingPathButUnsupportedExtension()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Index.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        List<String> supportedExtensions = new ArrayList<String>();
        supportedExtensions.add(View.Extension.JSF);

        try
        {
            new MockedViewConfigPathValidator(false).validateViewConfigPaths(null, viewConfigResolver, supportedExtensions);
        }
        catch (Exception e)
        {
            Assert.fail("unsupported extension wasn't ignored");
        }
    }

    private class MockedViewConfigPathValidator extends ViewConfigPathValidator
    {
        private final boolean validatePathAsValid;

        private MockedViewConfigPathValidator(boolean validatePathAsValid)
        {
            this.validatePathAsValid = validatePathAsValid;
        }

        @Override
        public void validateViewConfigPaths(ServletContextEvent sce,
                                            ViewConfigResolver viewConfigResolver,
                                            List<String> supportedExtensions)
        {
            super.validateViewConfigPaths(sce, viewConfigResolver, supportedExtensions);
        }

        @Override
        protected boolean isValidPath(ServletContextEvent sce, ConfigDescriptor configDescriptor)
        {
            //in our tests we just validate views -> skip folders
            return !configDescriptor.getMetaData(Folder.class).isEmpty() || this.validatePathAsValid;
        }

        @Override
        protected void printException(Exception e)
        {
            //do nothing
        }
    }
}
