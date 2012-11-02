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
package org.apache.deltaspike.test.jsf.impl.config.view.controller.uc004;

import org.apache.deltaspike.core.api.config.view.metadata.CallbackDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.config.view.metadata.annotation.DefaultCallback;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.api.config.view.controller.InitView;
import org.apache.deltaspike.jsf.api.config.view.controller.PageBean;
import org.apache.deltaspike.jsf.api.config.view.controller.PreRenderView;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigExtension;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Tests for view-configs
 */
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
    public void testSimpleMetaDataTreeWithViewControllerCallback()
    {
        this.viewConfigExtension.addPageDefinition(SimplePageConfig.class);

        ViewConfigNode node = this.viewConfigExtension.findNode(SimplePageConfig.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNull(node.getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(2, node.getMetaData().size());

        Iterator<Annotation> metaDataIterator = node.getMetaData().iterator();
        List<Class<? extends Annotation>> possibleMetaDataTypes = new ArrayList<Class<? extends Annotation>>();
        possibleMetaDataTypes.add(PageBean.class);
        possibleMetaDataTypes.add(TestSecured.class);
        Class<? extends Annotation> foundMetaData = metaDataIterator.next().annotationType();
        possibleMetaDataTypes.remove(foundMetaData);
        foundMetaData = metaDataIterator.next().annotationType();
        possibleMetaDataTypes.remove(foundMetaData);

        Assert.assertTrue(possibleMetaDataTypes.isEmpty());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());

        Assert.assertNotNull(node.getCallbackDescriptors());
        //TODO related to the discussion about #getInheritedMetaData (see TODOs in other use-cases)
        Assert.assertEquals(0, node.getCallbackDescriptors().size()); //get added directly before adding the meta-data
    }

    @Test
    public void testSimpleViewConfigWithCallbacks()
    {
        this.viewConfigExtension.addPageDefinition(SimplePageConfig.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigExtension.createViewConfigResolver();
        ViewConfigDescriptor viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(SimplePageConfig.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertNull(viewConfigDescriptor.getCallbackDescriptor(PageBean.class, InitView.class));
        Assert.assertNotNull(viewConfigDescriptor.getCallbackDescriptor(PageBean.class, PreRenderView.class));
        Assert.assertNotNull(viewConfigDescriptor.getCallbackDescriptor(TestSecured.class, DefaultCallback.class));
    }

    @Test
    public void testCallbackExecution()
    {
        this.viewConfigExtension.addPageDefinition(SimplePageConfig.class);

        final SimpleTestAccessDecisionVoter testInstance = new SimpleTestAccessDecisionVoter();

        ViewConfigNode node = this.viewConfigExtension.findNode(SimplePageConfig.class);
        //add it to avoid in-container test for this simple constellation - usually not needed!
        node.getCallbackDescriptors().put(TestSecured.class, new ArrayList<CallbackDescriptor>() {{
            add(new TestSecured.TestSecuredDescriptor(new Class[] {SimpleTestAccessDecisionVoter.class}, DefaultCallback.class) {
                @Override
                protected Object getTargetObject(Class targetType)
                {
                    return testInstance;
                }
            });
        }});

        ViewConfigResolver viewConfigResolver = this.viewConfigExtension.createViewConfigResolver();
        ViewConfigDescriptor viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(SimplePageConfig.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertNotNull(viewConfigDescriptor.getCallbackDescriptor(TestSecured.class, DefaultCallback.class));
        List<Set<String> /*return type of one callback*/> callbackResult =
            viewConfigDescriptor.getCallbackDescriptor(TestSecured.class, DefaultCallback.class, TestSecured.TestSecuredDescriptor.class)
                .execute("param1", "param2");
        Assert.assertNotNull(callbackResult);
        Assert.assertEquals(1, callbackResult.size());
        Assert.assertEquals(2, callbackResult.iterator().next().size());
        Iterator<String> resultIterator = callbackResult.iterator().next().iterator();
        Assert.assertEquals("param1", resultIterator.next());
        Assert.assertEquals("param2", resultIterator.next());
    }
}
