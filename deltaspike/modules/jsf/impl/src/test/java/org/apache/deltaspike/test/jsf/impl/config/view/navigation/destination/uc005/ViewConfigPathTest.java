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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.destination.uc005;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigExtension;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigResolverProducer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ViewConfigPathTest
{
    private static boolean active;

    private ViewConfigExtension viewConfigExtension;

    private ViewConfigResolverProducer viewConfigResolverProducer;

    @BeforeClass
    public static void init()
    {
        active = true;

        ConfigResolver.addConfigSources(new ArrayList<ConfigSource>() {
            {
                add(new ConfigSource() {
                    @Override
                    public int getOrdinal() {
                        return Integer.MAX_VALUE;
                    }

                    @Override
                    public Map<String, String> getProperties() {
                        return Collections.emptyMap();
                    }

                    @Override
                    public String getPropertyValue(String key) {
                        if (active && View.ViewConfigPreProcessor.class.getName().equals(key)) {
                            return ViewConfigPreProcessorWithoutValidation.class.getName();
                        }
                        return null;
                    }

                    @Override
                    public String getConfigName() {
                        return "test-view-config";
                    }

                    @Override
                    public boolean isScannable() {
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
    public void testNamesWizard1()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard1.Step1.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard1.Step2.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard1.Step3.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard1.Step4.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard1.Step5.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard1.Step6.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard1.Step7.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard1.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard1/step1.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard1.Step2.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/step2.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard1.Step3.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard1/step3.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard1.Step4.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard1/w1/step4.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard1.Step5.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/w1/step5.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard1.Step6.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard1/w1b/step6.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard1.Step7.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard1/w1b/step7.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard2()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard2.Step1.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard2.Step2.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard2.Step3.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard2.Step4.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard2.Step5.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard2.Step6.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard2.Step7.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard2.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard2/step1.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard2.Step2.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/step2.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard2.Step3.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard2/step3.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard2.Step4.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard2/w2/step4.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard2.Step5.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/w2/step5.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard2.Step6.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard2/w2b/step6.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard2.Step7.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard2/w2b/step7.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard3()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard3.Step1.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard3.Step2.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard3.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard3/step1.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard3.Step2.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/step2.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard4()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard4.Step1.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard4.Step2.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard4.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard4/step1.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard4.Step2.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/step2.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard5()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard5.Step1.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard5.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard5/step1.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard6()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard6.Step1.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard6.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard6/step1.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard7()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard7.Step1.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard7.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/wizard7/step1.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard8()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard8.Step1.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard8.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w8/step1.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard9()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard9.Step1.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard9.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/w9/step1.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard10()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard10.Step1.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard10.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w10a/step1.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard11()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard11.Step1.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard11.Step2.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard11.Step3.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard11.Step4.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard11.Step5.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard11.Step6.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard11.Step7.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard11.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w11/step1.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard11.Step2.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/step2.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard11.Step3.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w11/step3.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard11.Step4.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w11/w11b/step4.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard11.Step5.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/w11b/step5.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard11.Step6.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w11/w11b/step6.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard11.Step7.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w11/w11b/step7.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard12()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard12.Step1.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard12.Step2.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard12.Step3.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard12.Step4.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard12.Step5.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard12.Step6.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard12.Step7.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard12.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/w12/step1.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard12.Step2.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/step2.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard12.Step3.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/w12/step3.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard12.Step4.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/w12/w12b/step4.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard12.Step5.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/w12b/step5.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard12.Step6.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/w12/w12b/step6.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard12.Step7.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/w12/w12b/step7.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test
    public void testNamesWizard13()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard13.Step1.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard13.Step2.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard13.Step3.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard13.Step4.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard13.Step5.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard13.Step6.class);
        this.viewConfigExtension.addPageDefinition(Pages.Wizard13.Step7.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        ViewConfigDescriptor viewConfigDescriptor;


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard13.Step1.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w13a/step1.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard13.Step2.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/step2.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard13.Step3.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w13a/step3.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard13.Step4.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w13a/w13b/step4.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard13.Step5.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/w13b/step5.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard13.Step6.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w13a/w13b/step6.xhtml", viewConfigDescriptor.getViewId());

        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Wizard13.Step7.class);
        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/w13a/w13b/step7.xhtml", viewConfigDescriptor.getViewId());
    }

    @Test(expected = IllegalStateException.class)
    public void testNamesWizard14()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Wizard14.Step1.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();

        viewConfigResolver.getViewConfigDescriptor(Pages.Wizard14.Step1.class);
    }
}
