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
package org.apache.deltaspike.jsf.spi.scope.window;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;
import org.apache.deltaspike.jsf.api.config.base.JsfBaseConfig;
import org.apache.deltaspike.jsf.util.ValueExpressionEvaluationInputStream;

/**
 * <p>Default implementation of {@link ClientWindowConfig}.
 * By default it will use the internal <code>windowhandler.html</code></p>
 *
 * <p>You can &#064;Specializes this class to tweak the configuration or
 * provide a completely new implementation as &#064;Alternative.</p>
 */
@SessionScoped
public class DefaultClientWindowConfig implements ClientWindowConfig
{
    private static final long serialVersionUID = -708423418378550210L;

    /**
     * The location of the default windowhandler resource
     */
    private static final String DEFAULT_WINDOW_HANDLER_HTML_FILE = "static/windowhandler.html";


    private volatile Boolean javaScriptEnabled = null;

    /**
     * lazily initiated via {@link #getUserAgent(javax.faces.context.FacesContext)}
     */
    private volatile String userAgent = null;

    /**
     * Contains the cached ClientWindow handler html for this session.
     */
    private String clientWindowtml;

    @Inject
    private JsfModuleConfig jsfModuleConfig;

    @Inject
    private ProjectStage projectStage;

    private ClientWindowRenderMode defaultClientWindowRenderMode;
    private int maxWindowContextCount;

    @PostConstruct
    protected void init()
    {
        this.defaultClientWindowRenderMode = this.jsfModuleConfig.getDefaultWindowMode();
        this.maxWindowContextCount = JsfBaseConfig.ScopeCustomization.WindowRestriction.MAX_COUNT;
    }

    @Override
    public boolean isJavaScriptEnabled()
    {
        if (javaScriptEnabled == null)
        {
            synchronized (this)
            {
                // double lock checking idiom on volatile variable works since java5
                if (javaScriptEnabled == null)
                {
                    // no info means that it is default -> true
                    javaScriptEnabled = Boolean.TRUE;
                }
            }
        }
        return javaScriptEnabled;
    }


    @Override
    public void setJavaScriptEnabled(boolean javaScriptEnabled)
    {
        this.javaScriptEnabled = Boolean.valueOf(javaScriptEnabled);
    }

    /**
     * By default we use {@link ClientWindowRenderMode#LAZY} unless
     * we detect a bot. Use {@link org.apache.deltaspike.jsf.api.config.JsfModuleConfig#getDefaultWindowMode()}
     * to change this default behavior. Alternative:
     * Override this method to exclude other requests from getting accessed.
     */
    @Override
    public ClientWindowRenderMode getClientWindowRenderMode(FacesContext facesContext)
    {
        if (!isJavaScriptEnabled())
        {
            if (this.defaultClientWindowRenderMode != null)
            {
                return this.defaultClientWindowRenderMode; //currently mainly needed for 'DELEGATED'
            }
            return ClientWindowRenderMode.NONE;
        }

        String userAgent = getUserAgent(facesContext);

        if (userAgent != null &&
            ( userAgent.indexOf("bot")     >= 0 || // Googlebot, etc
              userAgent.indexOf("Bot")     >= 0 || // BingBot, etc
              userAgent.indexOf("Slurp")   >= 0 || // Yahoo Slurp
              userAgent.indexOf("Crawler") >= 0    // various other Crawlers
            ) )
        {
            return ClientWindowRenderMode.NONE;
        }

        if (this.defaultClientWindowRenderMode != null)
        {
            return this.defaultClientWindowRenderMode;
        }
        return ClientWindowRenderMode.LAZY;
    }

    @Override
    public String getClientWindowHtml()
    {
        if (projectStage != ProjectStage.Development && clientWindowtml != null)
        {
            // use cached windowHandlerHtml except in Development
            return clientWindowtml;
        }

        InputStream is = ClassUtils.getClassLoader(null).getResourceAsStream(getClientWindowResourceLocation());
        // wrap InputStream to evaluate EL expressions like resource includes
        is = new ValueExpressionEvaluationInputStream(FacesContext.getCurrentInstance(), is);
        StringBuffer sb = new StringBuffer();
        try
        {
            byte[] buf = new byte[16 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buf)) != -1)
            {
                String sbuf = new String(buf, 0, bytesRead);
                sb.append(sbuf);
            }
        }
        catch (IOException e)
        {
            ExceptionUtils.throwAsRuntimeException(e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                // do nothing, all fine so far
            }
        }

        clientWindowtml = sb.toString();

        return clientWindowtml;
    }

    /**
     * This information will get stored as it cannot
     * change during the session anyway.
     * @return the UserAgent of the request.
     */
    public String getUserAgent(FacesContext facesContext)
    {
        if (userAgent == null)
        {
            synchronized (this)
            {
                if (userAgent == null)
                {
                    Map<String, String[]> requestHeaders =
                            facesContext.getExternalContext().getRequestHeaderValuesMap();

                    if (requestHeaders != null &&
                            requestHeaders.containsKey("User-Agent"))
                    {
                        String[] userAgents = requestHeaders.get("User-Agent");
                        userAgent = userAgents.length > 0 ? userAgents[0] : null;
                    }
                }
            }
        }

        return userAgent;
    }


    /**
     * Overwrite this to define your own ClientWindow handler html location.
     * This will get picked up as resource from the classpath.
     */
    public String getClientWindowResourceLocation()
    {
        return DEFAULT_WINDOW_HANDLER_HTML_FILE;
    }

    @Override
    public int getMaxWindowContextCount()
    {
        return this.maxWindowContextCount;
    }

    @Override
    public boolean isClientWindowStoreWindowTreeEnabledOnLinkClick()
    {
        return true;
    }

    @Override
    public boolean isClientWindowStoreWindowTreeEnabledOnButtonClick()
    {
        return false;
    }
    
    @Override
    public boolean isClientWindowTokenizedRedirectEnabled()
    {
        return false;
    }
}
