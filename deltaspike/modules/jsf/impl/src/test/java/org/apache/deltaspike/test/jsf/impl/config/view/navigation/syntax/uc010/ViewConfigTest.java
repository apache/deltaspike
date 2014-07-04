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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.syntax.uc010;

import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigExtension;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigResolverProducer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;

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
    public void testMetaDataTreeWithStereotypeMetaData()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Home.class);
        this.viewConfigExtension.addPageDefinition(Pages.Public.Index.class);

        ViewConfigNode node = this.viewConfigExtension.findNode(Pages.Home.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNotNull(node.getParent().getParent());
        Assert.assertNull(node.getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(2, node.getMetaData().size());

        Assert.assertEquals(2, node.getMetaData().size());

        boolean facesRedirectAnnotationFound = false;
        boolean viewAnnotationFound = false;

        for (Annotation metaData : node.getMetaData())
        {
            if (MyView.class.isAssignableFrom(metaData.annotationType()))
            {
                facesRedirectAnnotationFound = true;
            }
            else if (View.class.isAssignableFrom(metaData.annotationType()))
            {
                viewAnnotationFound = true;
            }
        }

        Assert.assertTrue(facesRedirectAnnotationFound);
        Assert.assertTrue(viewAnnotationFound);

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());

        Assert.assertNotNull(node.getCallbackDescriptors());
        Assert.assertEquals(0, node.getCallbackDescriptors().size());


        node = this.viewConfigExtension.findNode(Pages.Public.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNotNull(node.getParent().getParent());
        Assert.assertNull(node.getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(1, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(1, node.getMetaData().size());
        Assert.assertEquals(MyView.class, node.getMetaData().iterator().next().annotationType());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());

        Assert.assertNotNull(node.getCallbackDescriptors());
        Assert.assertEquals(0, node.getCallbackDescriptors().size());


        node = this.viewConfigExtension.findNode(Pages.Public.Index.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNotNull(node.getParent().getParent());
        Assert.assertNotNull(node.getParent().getParent().getParent());
        Assert.assertNull(node.getParent().getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());

        Assert.assertNotNull(node.getCallbackDescriptors());
        Assert.assertEquals(0, node.getCallbackDescriptors().size());
    }

    @Test
    public void testViewConfigWithStereotypeMetaData()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Home.class);
        this.viewConfigExtension.addPageDefinition(Pages.Public.Index.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();


        ViewConfigDescriptor viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Home.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertNotNull(viewConfigDescriptor.getMetaData());
        Assert.assertEquals(2, viewConfigDescriptor.getMetaData().size());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData(View.class).size());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData(MyView.class).size());
        Assert.assertEquals(View.NavigationMode.REDIRECT, viewConfigDescriptor.getMetaData(View.class).iterator().next().navigation());
        Assert.assertEquals(View.ViewParameterMode.INCLUDE, viewConfigDescriptor.getMetaData(View.class).iterator().next().viewParams());
        Assert.assertEquals("landing page", viewConfigDescriptor.getMetaData(MyView.class).iterator().next().description());


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Public.Index.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertNotNull(viewConfigDescriptor.getMetaData());
        Assert.assertEquals(2, viewConfigDescriptor.getMetaData().size());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData(View.class).size());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData(MyView.class).size());
        Assert.assertEquals(View.NavigationMode.REDIRECT, viewConfigDescriptor.getMetaData(View.class).iterator().next().navigation());
        Assert.assertEquals(View.ViewParameterMode.EXCLUDE, viewConfigDescriptor.getMetaData(View.class).iterator().next().viewParams());
        Assert.assertEquals("public content", viewConfigDescriptor.getMetaData(MyView.class).iterator().next().description());
    }
}
