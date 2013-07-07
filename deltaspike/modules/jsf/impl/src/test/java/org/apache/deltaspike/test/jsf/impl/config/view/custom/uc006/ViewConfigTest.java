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
package org.apache.deltaspike.test.jsf.impl.config.view.custom.uc006;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigExtension;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigResolverProducer;
import org.junit.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ViewConfigTest
{
    private ViewConfigExtension viewConfigExtension;

    private ViewConfigResolverProducer viewConfigResolverProducer;

    private static boolean active;

    @BeforeClass
    public static void init()
    {
        active = true;

        ConfigResolver.addConfigSources(new ArrayList<ConfigSource>() {
            {
                add(new ConfigSource()
                {
                    @Override
                    public int getOrdinal()
                    {
                        return Integer.MAX_VALUE;
                    }

                    @Override
                    public Map<String, String> getProperties()
                    {
                        return Collections.emptyMap();
                    }

                    @Override
                    public String getPropertyValue(String key)
                    {
                        if (active && View.ViewConfigPreProcessor.class.getName().equals(key))
                        {
                            return TestConfigPreProcessor.class.getName();
                        }
                        return null;
                    }

                    @Override
                    public String getConfigName()
                    {
                        return "test-view-config";
                    }

                    @Override
                    public boolean isScannable()
                    {
                        return false;
                    }
                });
            }

            private static final long serialVersionUID = 3247551986947387154L;
        });
    }

    @AfterClass
    public static void cleanup()
    {
        active = false;
    }

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
    public void testMetaDataTreeCustomViewConfigPreProcessor()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Public.Index.class);

        ViewConfigNode node = this.viewConfigExtension.findNode(Pages.Public.Index.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNotNull(node.getParent().getParent());
        Assert.assertNotNull(node.getParent().getParent().getParent());
        Assert.assertNull(node.getParent().getParent().getParent().getParent());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(1, node.getMetaData().size());
        Assert.assertEquals(View.class, node.getMetaData().iterator().next().annotationType());
        Assert.assertEquals(View.ViewParameterMode.INCLUDE, ((View)node.getMetaData().iterator().next()).viewParams());
    }

    @Test
    public void testViewConfigCustomViewConfigPreProcessor()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Public.Index.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();
        ViewConfigDescriptor viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Public.Index.class);

        //changed by TestConfigPreProcessor
        Assert.assertEquals("/test/view.custom", viewConfigDescriptor.getViewId());
        Assert.assertEquals(View.ViewParameterMode.DEFAULT, viewConfigDescriptor.getMetaData(View.class).iterator().next().viewParams());
    }
}
