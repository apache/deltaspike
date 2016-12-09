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
package org.apache.deltaspike.cdise.owb;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.enterprise.inject.Typed;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 * Mock ServletContext needed to startup the container.
 *
 */
@Typed // ignore this as CDI bean
public class MockServletContext implements ServletContext
{
    private static MockServletContext instance = new MockServletContext();


    private Hashtable attributes = new Hashtable();


    private MockServletContext()
    {
        // this class is only accessible via getInstance
    }

    public static synchronized MockServletContext getInstance()
    {
        return instance;
    }

    public static ServletContextEvent getServletContextEvent()
    {
        return new ServletContextEvent(getInstance());
    }






    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    public Enumeration getAttributeNames()
    {
        return attributes.keys();
    }

    public ServletContext getContext(String uripath)
    {
        return this;
    }

    public String getContextPath()
    {
        return "mockContextpath";
    }

    public String getInitParameter(String name)
    {
        return null;
    }

    public Enumeration getInitParameterNames()
    {
        return new StringTokenizer(""); // 'standard' empty Enumeration
    }

    public int getMajorVersion()
    {
        return 2;
    }

    public String getMimeType(String file)
    {
        return null;
    }

    public int getMinorVersion()
    {
        return 0;
    }

    public RequestDispatcher getNamedDispatcher(String name)
    {
        return null;
    }

    public String getRealPath(String path)
    {
        return "mockRealPath";
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
        return null;
    }

    public URL getResource(String path) throws MalformedURLException
    {
        return null;
    }

    public InputStream getResourceAsStream(String path)
    {
        return null;
    }

    public Set getResourcePaths(String path)
    {
        return null;
    }

    public String getServerInfo()
    {
        return "mockServer";
    }

    public Servlet getServlet(String name) throws ServletException
    {
        return null;
    }

    public String getServletContextName()
    {
        return null;
    }

    public Enumeration getServletNames()
    {
        return null;
    }

    public Enumeration getServlets()
    {
        return null;
    }

    public void log(String msg)
    {
        // nothing to do
    }

    public void log(Exception exception, String msg)
    {
        // nothing to do
    }

    public void log(String message, Throwable throwable)
    {
        // nothing to do
    }

    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    @SuppressWarnings("unchecked")
    public void setAttribute(String name, Object object)
    {
        attributes.put(name, object);
    }

    @Override
    public boolean setInitParameter(String name, String value)
    {
        return false;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className)
        throws IllegalArgumentException, IllegalStateException
    {
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet)
        throws IllegalArgumentException, IllegalStateException
    {
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> clazz)
        throws IllegalArgumentException, IllegalStateException
    {
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException
    {
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName)
    {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations()
    {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className)
        throws IllegalArgumentException, IllegalStateException
    {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter)
        throws IllegalArgumentException, IllegalStateException
    {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass)
        throws IllegalArgumentException, IllegalStateException
    {
        return null;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException
    {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName)
    {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations()
    {
        return null;
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass)
    {

    }

    @Override
    public void addListener(String className)
    {

    }

    @Override
    public <T extends EventListener> void addListener(T t)
    {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException
    {
        return null;
    }

    @Override
    public void declareRoles(String... roleNames)
    {

    }

    @Override
    public SessionCookieConfig getSessionCookieConfig()
    {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
    {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
    {
        return null;
    }

    @Override
    public int getEffectiveMajorVersion() throws UnsupportedOperationException
    {
        return 0;
    }

    @Override
    public int getEffectiveMinorVersion() throws UnsupportedOperationException
    {
        return 0;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
    {
        return null;
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor()
    {
        return null;
    }
}
