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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.destination.uc004;

import org.apache.deltaspike.core.api.config.view.DefaultErrorView;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigExtension;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigResolverProducer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ViewConfigTest
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
    public void testDuplicatedDefaultErrorViewInMetaDataTree()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Index.class);
        this.viewConfigExtension.addPageDefinition(Pages.Overview.class);

        ViewConfigNode node = this.viewConfigExtension.findNode(Pages.Index.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNotNull(node.getParent().getParent());
        Assert.assertNull(node.getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());


        node = this.viewConfigExtension.findNode(Pages.Overview.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNotNull(node.getParent().getParent());
        Assert.assertNull(node.getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());
    }

    @Test(expected = IllegalStateException.class)
    public void testDuplicatedDefaultErrorViewDuringPageMetaDataTransformation()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Index.class);
        this.viewConfigExtension.addPageDefinition(Pages.Overview.class);

        this.viewConfigResolverProducer.createViewConfigResolver();
    }

    @Test
    public void testDefaultErrorViewInViewConfig()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Index.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        Assert.assertNotNull(viewConfigResolver.getDefaultErrorViewConfigDescriptor());
        Assert.assertEquals(Pages.Index.class, viewConfigResolver.getDefaultErrorViewConfigDescriptor().getConfigClass());
        Assert.assertEquals(Pages.Index.class, viewConfigResolver.getViewConfigDescriptor(DefaultErrorView.class).getConfigClass());
    }
}
