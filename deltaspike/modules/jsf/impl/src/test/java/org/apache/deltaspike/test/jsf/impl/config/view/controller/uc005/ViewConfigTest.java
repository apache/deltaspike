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
package org.apache.deltaspike.test.jsf.impl.config.view.controller.uc005;

import org.apache.deltaspike.core.api.config.view.controller.InitView;
import org.apache.deltaspike.core.api.config.view.controller.PreRenderView;
import org.apache.deltaspike.core.api.config.view.controller.ViewControllerRef;
import org.apache.deltaspike.core.api.config.view.metadata.*;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigExtension;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigResolverProducer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Tests for view-configs
 */
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
    public void testMetaDataTreeWithStereotypeViewMetaData()
    {
        this.viewConfigExtension.addPageDefinition(Pages.class);
        this.viewConfigExtension.addPageDefinition(Pages.Secure.class);
        this.viewConfigExtension.addPageDefinition(Pages.Secure.Settings.class);

        ViewConfigNode node = this.viewConfigExtension.findNode(Pages.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNull(node.getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(1, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());

        Assert.assertNotNull(node.getCallbackDescriptors());
        Assert.assertEquals(0, node.getCallbackDescriptors().size());


        node = this.viewConfigExtension.findNode(Pages.Secure.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNotNull(node.getParent().getParent());
        Assert.assertNull(node.getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(1, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(1, node.getMetaData().size());

        Iterator<Annotation> metaDataIterator = node.getMetaData().iterator();
        List<Class<? extends Annotation>> possibleMetaDataTypes = new ArrayList<Class<? extends Annotation>>();
        possibleMetaDataTypes.add(SecuredStereotype1.class);
        Class<? extends Annotation> foundMetaData = metaDataIterator.next().annotationType();
        possibleMetaDataTypes.remove(foundMetaData);

        Assert.assertTrue(possibleMetaDataTypes.isEmpty());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());

        Assert.assertNotNull(node.getCallbackDescriptors());
        Assert.assertEquals(0, node.getCallbackDescriptors().size());


        node = this.viewConfigExtension.findNode(Pages.Secure.Settings.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNotNull(node.getParent().getParent());
        Assert.assertNotNull(node.getParent().getParent().getParent());
        Assert.assertNull(node.getParent().getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(2, node.getMetaData().size());

        metaDataIterator = node.getMetaData().iterator();
        possibleMetaDataTypes = new ArrayList<Class<? extends Annotation>>();
        possibleMetaDataTypes.add(ViewControllerRef.class);
        possibleMetaDataTypes.add(SecuredStereotype2.class);
        foundMetaData = metaDataIterator.next().annotationType();
        possibleMetaDataTypes.remove(foundMetaData);
        foundMetaData = metaDataIterator.next().annotationType();
        possibleMetaDataTypes.remove(foundMetaData);

        Assert.assertTrue(possibleMetaDataTypes.isEmpty());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());

        Assert.assertNotNull(node.getCallbackDescriptors());
        Assert.assertEquals(0, node.getCallbackDescriptors().size());
    }

    @Test
    public void testBaseViewConfigWithStereotypeViewMetaData()
    {
        this.viewConfigExtension.addPageDefinition(Pages.class);
        this.viewConfigExtension.addPageDefinition(Pages.Secure.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();
        ConfigDescriptor configDescriptor = viewConfigResolver.getConfigDescriptor(Pages.Secure.class);

        Assert.assertNotNull(configDescriptor);
        Assert.assertNotNull(configDescriptor.getCallbackDescriptor(TestSecured.class));
        Assert.assertNotNull(configDescriptor.getCallbackDescriptor(TestSecured.class).getCallbackMethods());
        Assert.assertFalse(configDescriptor.getCallbackDescriptor(TestSecured.class).getCallbackMethods().isEmpty());
        Assert.assertNotNull(configDescriptor.getCallbackDescriptor(TestSecured.class).getCallbackMethods().get(SimpleTestAccessDecisionVoter1.class));
    }

    @Test
    public void testViewConfigWithStereotypeViewMetaData()
    {
        this.viewConfigExtension.addPageDefinition(Pages.class);
        this.viewConfigExtension.addPageDefinition(Pages.Secure.class);
        this.viewConfigExtension.addPageDefinition(Pages.Secure.Settings.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();
        ViewConfigDescriptor viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Secure.Settings.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertNull(viewConfigDescriptor.getCallbackDescriptor(ViewControllerRef.class, InitView.class));
        Assert.assertNotNull(viewConfigDescriptor.getCallbackDescriptor(ViewControllerRef.class, PreRenderView.class));
        Assert.assertNotNull(viewConfigDescriptor.getCallbackDescriptor(TestSecured.class));
        Assert.assertNotNull(viewConfigDescriptor.getCallbackDescriptor(TestSecured.class).getCallbackMethods());
        Assert.assertFalse(viewConfigDescriptor.getCallbackDescriptor(TestSecured.class).getCallbackMethods().isEmpty());
        Assert.assertNotNull(viewConfigDescriptor.getCallbackDescriptor(TestSecured.class).getCallbackMethods().get(SimpleTestAccessDecisionVoter2.class));
    }

    @Test
    public void testCallbackExecutionFolder()
    {
        this.viewConfigExtension.addPageDefinition(Pages.class);
        this.viewConfigExtension.addPageDefinition(Pages.Secure.class);

        final SimpleTestAccessDecisionVoter1 testInstance1 = new SimpleTestAccessDecisionVoter1();

        ViewConfigNode node = this.viewConfigExtension.findNode(Pages.Secure.class);
        //add it to avoid in-container test for this simple constellation - usually not needed!
        node.getCallbackDescriptors().put(TestSecured.class, new ArrayList<CallbackDescriptor>() {{
            add(new TestSecured.Descriptor(new Class[] {SimpleTestAccessDecisionVoter1.class}, DefaultCallback.class) {
                @Override
                protected Object getTargetObject(Class targetType)
                {
                    return testInstance1;
                }
            });
        }});

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();
        ConfigDescriptor configDescriptor = viewConfigResolver.getConfigDescriptor(Pages.Secure.class);

        Assert.assertNotNull(configDescriptor);
        Assert.assertNotNull(configDescriptor.getCallbackDescriptor(TestSecured.class));
        List<Set<String> /*return type of one callback*/> callbackResult =
                ((TestSecured.Descriptor)configDescriptor.getExecutableCallbackDescriptor(TestSecured.class, TestSecured.Descriptor.class))
                        .execute("param1", "param2");
        Assert.assertNotNull(callbackResult);
        Assert.assertEquals(1, callbackResult.size());
        Assert.assertEquals(3, callbackResult.iterator().next().size());
        Iterator<String> resultIterator = callbackResult.iterator().next().iterator();

        //the order in the result isn't guaranteed
        Set<String> expectedValues = new CopyOnWriteArraySet<String>();
        expectedValues.add("param1");
        expectedValues.add("param2");
        expectedValues.add(SimpleTestAccessDecisionVoter1.class.getName());

        while (resultIterator.hasNext())
        {
            String currentValue = resultIterator.next();
            if (!expectedValues.remove(currentValue))
            {
                Assert.fail("value '" + currentValue + "' not found in the result");
            }
        }
        Assert.assertTrue(expectedValues.isEmpty());
    }

    @Test
    public void testCallbackExecutionPage()
    {
        this.viewConfigExtension.addPageDefinition(Pages.class);
        this.viewConfigExtension.addPageDefinition(Pages.Secure.class);
        this.viewConfigExtension.addPageDefinition(Pages.Secure.Settings.class);

        final SimpleTestAccessDecisionVoter2 testInstance2 = new SimpleTestAccessDecisionVoter2();

        ViewConfigNode node = this.viewConfigExtension.findNode(Pages.Secure.Settings.class);
        //add it to avoid in-container test for this simple constellation - usually not needed!
        node.getCallbackDescriptors().put(TestSecured.class, new ArrayList<CallbackDescriptor>() {{
            add(new TestSecured.Descriptor(new Class[] {SimpleTestAccessDecisionVoter2.class}, DefaultCallback.class) {
                @Override
                protected Object getTargetObject(Class targetType)
                {
                    return testInstance2;
                }
            });
        }});

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();
        ViewConfigDescriptor viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Secure.Settings.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertNotNull(viewConfigDescriptor.getCallbackDescriptor(TestSecured.class));
        List<Set<String> /*return type of one callback*/> callbackResult =
            viewConfigDescriptor.getExecutableCallbackDescriptor(TestSecured.class, TestSecured.Descriptor.class)
                .execute("param1", "param2");
        Assert.assertNotNull(callbackResult);
        Assert.assertEquals(1, callbackResult.size());
        Assert.assertEquals(3, callbackResult.iterator().next().size());
        Iterator<String> resultIterator = callbackResult.iterator().next().iterator();

        //the order in the result isn't guaranteed
        Set<String> expectedValues = new HashSet<String>();
        expectedValues.add("param1");
        expectedValues.add("param2");
        expectedValues.add(SimpleTestAccessDecisionVoter2.class.getName());

        while (resultIterator.hasNext())
        {
            String currentValue = resultIterator.next();
            if (!expectedValues.remove(currentValue))
            {
                Assert.fail("value '" + currentValue + "' not found in the result");
            }
        }
        Assert.assertTrue(expectedValues.isEmpty());
    }
}
