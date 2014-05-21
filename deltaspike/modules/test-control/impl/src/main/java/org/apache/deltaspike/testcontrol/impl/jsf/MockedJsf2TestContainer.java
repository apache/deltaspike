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
package org.apache.deltaspike.testcontrol.impl.jsf;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.testcontrol.spi.ExternalContainer;
import org.apache.myfaces.test.mock.MockApplicationFactory;
import org.apache.myfaces.test.mock.MockExceptionHandlerFactory;
import org.apache.myfaces.test.mock.MockExternalContext;
import org.apache.myfaces.test.mock.MockFacesContext;
import org.apache.myfaces.test.mock.MockFacesContextFactory;
import org.apache.myfaces.test.mock.MockHttpServletRequest;
import org.apache.myfaces.test.mock.MockHttpServletResponse;
import org.apache.myfaces.test.mock.MockHttpSession;
import org.apache.myfaces.test.mock.MockPartialViewContextFactory;
import org.apache.myfaces.test.mock.MockRenderKit;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockServletConfig;
import org.apache.myfaces.test.mock.MockServletContext;
import org.apache.myfaces.test.mock.lifecycle.MockLifecycleFactory;
import org.apache.myfaces.test.mock.visit.MockVisitContextFactory;

import javax.el.ExpressionFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//known restriction: faces-config.xml files are ignored
@ApplicationScoped
public class MockedJsf2TestContainer implements ExternalContainer
{
    protected MockServletConfig servletConfig;
    protected MockServletContext servletContext;

    protected Lifecycle lifecycle;
    protected RenderKit renderKit;
    protected Application application;

    protected FacesContext facesContext;
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;
    protected MockHttpSession session;

    protected Map<String, String> containerConfig;

    public void boot()
    {
        initContainerConfig();
        initServletObjects();
        initJsf();
    }

    protected void initContainerConfig()
    {
        containerConfig = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : ConfigResolver.getAllProperties().entrySet())
        {
            if (entry.getKey().startsWith("org.apache.myfaces.") || entry.getKey().startsWith("javax.faces.") ||
                    entry.getKey().startsWith("facelets."))
            {
                containerConfig.put(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void initServletObjects()
    {
        this.servletContext = new MockServletContext();
        this.servletConfig = new MockServletConfig(this.servletContext);
        applyContainerConfig();
    }

    protected void applyContainerConfig()
    {
        //add the default values
        servletContext.addInitParameter("javax.faces.PROJECT_STAGE", "UnitTest");
        servletContext.addInitParameter("javax.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "-1");

        servletContext.addInitParameter("org.apache.myfaces.INITIALIZE_ALWAYS_STANDALONE", "true");
        servletContext.addInitParameter("org.apache.myfaces.spi.InjectionProvider",
            "org.apache.myfaces.spi.impl.NoInjectionAnnotationInjectionProvider");
        servletContext.addInitParameter("org.apache.myfaces.config.annotation.LifecycleProvider",
            "org.apache.myfaces.config.annotation.NoInjectionAnnotationLifecycleProvider");
        servletConfig.addInitParameter("org.apache.myfaces.CHECKED_VIEWID_CACHE_ENABLED", "false");

        servletContext.addInitParameter(ExpressionFactory.class.getName(), "org.apache.el.ExpressionFactoryImpl");
        //add custom values (might replace the default values)
        for (Map.Entry<String, String> entry : containerConfig.entrySet())
        {
            servletContext.addInitParameter(entry.getKey(), entry.getValue());
        }
    }

    protected void initJsf()
    {
        FactoryFinder.releaseFactories();

        onPreInitJsf();

        initLifecycle();
        initApplication();
        initRenderKit();
    }

    protected void onPreInitJsf()
    {
        //init mocked jsf factories
        addFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
        addFactory(FactoryFinder.FACES_CONTEXT_FACTORY, MockFacesContextFactory.class.getName());
        addFactory(FactoryFinder.LIFECYCLE_FACTORY, MockLifecycleFactory.class.getName());
        addFactory(FactoryFinder.RENDER_KIT_FACTORY, MockRenderKitFactory.class.getName());
        addFactory(FactoryFinder.EXCEPTION_HANDLER_FACTORY, MockExceptionHandlerFactory.class.getName());
        addFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY, MockPartialViewContextFactory.class.getName());
        addFactory(FactoryFinder.VISIT_CONTEXT_FACTORY, MockVisitContextFactory.class.getName());
    }

    protected void addFactory(String factoryName, String className)
    {
        FactoryFinder.setFactory(factoryName, className);
    }

    protected void initLifecycle()
    {
        LifecycleFactory lifecycleFactory =
                (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        this.lifecycle = lifecycleFactory.getLifecycle(getLifecycleId());
    }

    protected void initApplication()
    {
        ApplicationFactory applicationFactory =
                (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        this.application = applicationFactory.getApplication();
    }

    protected void initRenderKit()
    {
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder
                .getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        this.renderKit = new MockRenderKit();
        renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, this.renderKit);
    }

    @Override
    public void startScope(Class<? extends Annotation> scopeClass)
    {
        if (RequestScoped.class.equals(scopeClass))
        {
            initRequest();
            initResponse();

            initFacesContext();

            initDefaultView();
        }
        else if (SessionScoped.class.equals(scopeClass))
        {
            initSession();
        }
    }

    protected void initRequest()
    {
        this.request = new MockHttpServletRequest(this.session);
        this.request.setServletContext(this.servletContext);
    }

    protected void initResponse()
    {
        this.response = new MockHttpServletResponse();
    }

    protected void initFacesContext()
    {
        FacesContextFactory facesContextFactory =
                (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        this.facesContext = facesContextFactory.getFacesContext(
                this.servletContext, this.request, this.response, this.lifecycle);

        ((MockFacesContext) this.facesContext).setApplication(this.application);
        ExceptionHandler exceptionHandler = ((ExceptionHandlerFactory)
                FactoryFinder.getFactory(FactoryFinder.EXCEPTION_HANDLER_FACTORY)).getExceptionHandler();
        this.facesContext.setExceptionHandler(exceptionHandler);

        ((MockFacesContext) this.facesContext).setExternalContext(
                new MockExternalContext(this.servletContext, this.request, this.response));
    }

    protected void initDefaultView()
    {
        UIViewRoot root = new UIViewRoot();
        root.setViewId("/viewId");
        root.setLocale(getLocale());
        root.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        this.facesContext.setViewRoot(root);
    }

    protected void initSession()
    {
        if (this.request != null)
        {
            this.session = (MockHttpSession)this.request.getSession(true);
        }
        else
        {
            this.session = new MockHttpSession();
            this.session.setServletContext(this.servletContext);
        }
    }

    @Override
    public void stopScope(Class<? extends Annotation> scopeClass)
    {
        if (RequestScoped.class.equals(scopeClass))
        {
            if (this.facesContext != null)
            {
                this.facesContext.release();
            }
            this.facesContext = null;
            this.request = null;
            this.response = null;
        }
        else if (SessionScoped.class.equals(scopeClass))
        {
            this.session = null;
        }
    }

    protected Locale getLocale()
    {
        return Locale.getDefault();
    }

    public void shutdown()
    {
        this.application = null;
        this.servletConfig = null;
        this.containerConfig = null;
        this.lifecycle = null;
        this.renderKit = null;
        this.servletContext = null;

        FactoryFinder.releaseFactories();
    }

    @Override
    public int getOrdinal()
    {
        return 1000; //default in ds
    }

    protected String getLifecycleId()
    {
        return LifecycleFactory.DEFAULT_LIFECYCLE;
    }
}
