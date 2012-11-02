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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.syntax.uc003;

import org.apache.deltaspike.core.api.config.view.metadata.ConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.Page;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigExtension;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ViewConfigTest
{
    private ViewConfigExtension viewConfigExtension;

    @Before
    public void before()
    {
        this.viewConfigExtension = new ViewConfigExtension();
    }

    @After
    public void after()
    {
        this.viewConfigExtension.freeViewConfigCache(null);
    }

    @Test
    public void testNestedMetaDataTree()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Index.class);
        this.viewConfigExtension.addPageDefinition(Pages.Home.class);
        this.viewConfigExtension.addPageDefinition(Pages.Admin.Index.class);

        ViewConfigNode node = this.viewConfigExtension.findNode(Pages.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Root
        Assert.assertNull(node.getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(3, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());


        node = this.viewConfigExtension.findNode(Pages.Admin.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Admin
        Assert.assertNotNull(node.getParent().getParent()); //Root
        Assert.assertNull(node.getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(1, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());


        node = this.viewConfigExtension.findNode(Pages.Index.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Pages
        Assert.assertNotNull(node.getParent().getParent()); //Root
        Assert.assertNull(node.getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());


        node = this.viewConfigExtension.findNode(Pages.Home.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Pages
        Assert.assertNotNull(node.getParent().getParent()); //Root
        Assert.assertNull(node.getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(1, node.getMetaData().size());
        Assert.assertEquals(Page.NavigationMode.DEFAULT, ((Page) node.getMetaData().iterator().next()).navigation());
        Assert.assertEquals(Page.ViewParameterMode.DEFAULT, ((Page) node.getMetaData().iterator().next()).viewParams());
        Assert.assertEquals("", ((Page) node.getMetaData().iterator().next()).name());
        Assert.assertEquals(Page.Extension.DEFAULT, ((Page)node.getMetaData().iterator().next()).extension());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());


        node = this.viewConfigExtension.findNode(Pages.Admin.Index.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Admin
        Assert.assertNotNull(node.getParent().getParent()); //Pages
        Assert.assertNotNull(node.getParent().getParent().getParent()); //Root
        Assert.assertNull(node.getParent().getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());
    }

    @Test
    public void testNestedViewConfig()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Index.class);
        this.viewConfigExtension.addPageDefinition(Pages.Home.class);
        this.viewConfigExtension.addPageDefinition(Pages.Admin.Index.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigExtension.createViewConfigResolver();
        ConfigDescriptor configDescriptor = viewConfigResolver.getConfigDescriptor(Pages.class);

        Assert.assertNotNull(configDescriptor);
        Assert.assertNotNull(configDescriptor.getConfigClass());
        Assert.assertEquals(Pages.class, configDescriptor.getConfigClass());

        Assert.assertNotNull(configDescriptor.getMetaData());
        Assert.assertEquals(1, configDescriptor.getMetaData().size());
        Assert.assertEquals("/pages/", ((Folder)configDescriptor.getMetaData().iterator().next()).name());


        configDescriptor = viewConfigResolver.getConfigDescriptor(Pages.Admin.class);

        Assert.assertNotNull(configDescriptor);
        Assert.assertNotNull(configDescriptor.getConfigClass());
        Assert.assertEquals(Pages.Admin.class, configDescriptor.getConfigClass());

        Assert.assertNotNull(configDescriptor.getMetaData());
        Assert.assertEquals(1, configDescriptor.getMetaData().size());
        Assert.assertEquals("/pages/admin/", ((Folder)configDescriptor.getMetaData().iterator().next()).name());


        ViewConfigDescriptor viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Index.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/index.xhtml", viewConfigDescriptor.getViewId());
        Assert.assertEquals(Pages.Index.class, viewConfigDescriptor.getViewConfig());

        Assert.assertNotNull(viewConfigDescriptor.getMetaData());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData().size());
        Assert.assertEquals(Page.NavigationMode.FORWARD, ((Page) viewConfigDescriptor.getMetaData().iterator().next())
                .navigation());
        Assert.assertEquals(Page.ViewParameterMode.EXCLUDE, (viewConfigDescriptor.getMetaData(Page.class).iterator()
                .next()).viewParams());
        Assert.assertEquals("index", (viewConfigDescriptor.getMetaData(Page.class).iterator().next()).name());
        Assert.assertEquals(Page.Extension.XHTML, (viewConfigDescriptor.getMetaData(Page.class).iterator().next()).extension());


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Home.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/home.xhtml", viewConfigDescriptor.getViewId());
        Assert.assertEquals(Pages.Home.class, viewConfigDescriptor.getViewConfig());

        Assert.assertNotNull(viewConfigDescriptor.getMetaData());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData().size());
        Assert.assertEquals(Page.NavigationMode.FORWARD, ((Page) viewConfigDescriptor.getMetaData().iterator().next())
                .navigation());
        Assert.assertEquals(Page.ViewParameterMode.EXCLUDE, (viewConfigDescriptor.getMetaData(Page.class).iterator().next()).viewParams());
        Assert.assertEquals("home", (viewConfigDescriptor.getMetaData(Page.class).iterator().next()).name());
        Assert.assertEquals(Page.Extension.XHTML, (viewConfigDescriptor.getMetaData(Page.class).iterator().next()).extension());


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Admin.Index.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/admin/index.xhtml", viewConfigDescriptor.getViewId());
        Assert.assertEquals(Pages.Admin.Index.class, viewConfigDescriptor.getViewConfig());

        Assert.assertNotNull(viewConfigDescriptor.getMetaData());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData().size());
        Assert.assertEquals(Page.NavigationMode.FORWARD, ((Page) viewConfigDescriptor.getMetaData().iterator().next())
                .navigation());
        Assert.assertEquals(Page.ViewParameterMode.EXCLUDE, (viewConfigDescriptor.getMetaData(Page.class).iterator().next()).viewParams());
        Assert.assertEquals("index", (viewConfigDescriptor.getMetaData(Page.class).iterator().next()).name());
        Assert.assertEquals(Page.Extension.XHTML, (viewConfigDescriptor.getMetaData(Page.class).iterator().next()).extension());
    }
}
