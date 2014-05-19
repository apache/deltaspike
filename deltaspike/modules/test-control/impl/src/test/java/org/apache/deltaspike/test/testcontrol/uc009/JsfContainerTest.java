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
package org.apache.deltaspike.test.testcontrol.uc009;

import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.testcontrol.shared.RequestScopedBean;
import org.apache.deltaspike.test.testcontrol.shared.SessionScopedBean;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.render.RenderKitFactory;
import javax.inject.Inject;
import java.util.Map;

//Usually NOT needed! Currently only needed due to our arquillian-setup
@Category(SeCategory.class)



@RunWith(CdiTestRunner.class)
@TestControl(startExternalContainers = true)
public class JsfContainerTest
{
    private Integer identityHashCode;

    @Inject
    private SessionScopedBean sessionScopedBean;

    @Inject
    private RequestScopedBean requestScopedBean;

    @Test
    public void firstTest()
    {
        Assert.assertEquals(0, requestScopedBean.getCount());
        requestScopedBean.increaseCount();
        Assert.assertEquals(1, requestScopedBean.getCount());

        Assert.assertEquals(0, sessionScopedBean.getCount());
        sessionScopedBean.increaseCount();
        Assert.assertEquals(1, sessionScopedBean.getCount());

        UIViewRoot uiViewRoot = new UIViewRoot();
        uiViewRoot.setViewId("/viewId");
        FacesContext.getCurrentInstance().setViewRoot(uiViewRoot);
        Assert.assertNotNull(FacesContext.getCurrentInstance().getViewRoot());
        Assert.assertEquals("/viewId", FacesContext.getCurrentInstance().getViewRoot().getViewId());

        uiViewRoot.setViewId("/test1.xhtml");
        uiViewRoot.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        FacesContext.getCurrentInstance().setViewRoot(uiViewRoot);
        Assert.assertEquals("/test1.xhtml", FacesContext.getCurrentInstance().getViewRoot().getViewId());

        Assert.assertNotNull(FacesContext.getCurrentInstance().getExternalContext());
        Assert.assertNotNull(FacesContext.getCurrentInstance().getApplication());
        Assert.assertNotNull(FacesContext.getCurrentInstance().getELContext());
        Assert.assertNotNull(FacesContext.getCurrentInstance().getPartialViewContext());
        Assert.assertNotNull(FacesContext.getCurrentInstance().getRenderKit());
        Assert.assertNotNull(FacesContext.getCurrentInstance().getExceptionHandler());

        Assert.assertNull(FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("test"));
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("test", "1");
        Assert.assertEquals("1", FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("test"));

        Map applicationMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
        if (identityHashCode == null)
        {
            identityHashCode = System.identityHashCode(applicationMap);
        }
        else
        {
            Assert.assertSame(identityHashCode, System.identityHashCode(applicationMap));
        }
    }

    @Test
    public void secondTest()
    {
        Assert.assertEquals(0, requestScopedBean.getCount());
        requestScopedBean.increaseCount();
        Assert.assertEquals(1, requestScopedBean.getCount());

        Assert.assertEquals(0, sessionScopedBean.getCount());
        sessionScopedBean.increaseCount();
        Assert.assertEquals(1, sessionScopedBean.getCount());

        UIViewRoot uiViewRoot = new UIViewRoot();
        uiViewRoot.setViewId("/viewId");
        FacesContext.getCurrentInstance().setViewRoot(uiViewRoot);
        Assert.assertNotNull(FacesContext.getCurrentInstance().getViewRoot());
        Assert.assertEquals("/viewId", FacesContext.getCurrentInstance().getViewRoot().getViewId());

        uiViewRoot.setViewId("/test2.xhtml");
        uiViewRoot.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        FacesContext.getCurrentInstance().setViewRoot(uiViewRoot);

        FacesContext.getCurrentInstance().setViewRoot(uiViewRoot);
        Assert.assertEquals("/test2.xhtml", FacesContext.getCurrentInstance().getViewRoot().getViewId());

        Assert.assertNotNull(FacesContext.getCurrentInstance().getExternalContext());
        Assert.assertNotNull(FacesContext.getCurrentInstance().getApplication());
        Assert.assertNotNull(FacesContext.getCurrentInstance().getELContext());
        Assert.assertNotNull(FacesContext.getCurrentInstance().getPartialViewContext());
        Assert.assertNotNull(FacesContext.getCurrentInstance().getRenderKit());
        Assert.assertNotNull(FacesContext.getCurrentInstance().getExceptionHandler());

        Assert.assertNull(FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("test"));
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("test", "2");
        Assert.assertEquals("2", FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("test"));

        Map applicationMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
        if (identityHashCode == null)
        {
            identityHashCode = System.identityHashCode(applicationMap);
        }
        else
        {
            Assert.assertSame(identityHashCode, System.identityHashCode(applicationMap));
        }
    }
}
