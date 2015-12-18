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
package org.apache.deltaspike.jsf.impl.util;

import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.jsf.api.config.base.JsfBaseConfig;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;
import org.apache.deltaspike.jsf.impl.listener.phase.WindowMetaData;
import org.apache.deltaspike.jsf.impl.message.FacesMessageEntry;

import javax.el.ELException;
import javax.enterprise.context.ContextNotActiveException;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.deltaspike.core.util.StringUtils;

public abstract class JsfUtils
{
    private static final String SB_ADD_PARAMETER = "SB:" + JsfUtils.class + "#addParameter";

    public static <T> T getValueOfExpression(String expression, Class<T> targetType)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext.getApplication().evaluateExpressionGet(facesContext, expression, targetType);
    }

    public static String getValueOfExpressionAsString(String expression)
    {
        Object result = getValueOfExpression(expression, Object.class);

        return result != null ? result.toString() : "null";
    }

    public static Set<RequestParameter> getViewConfigPageParameters()
    {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

        Set<RequestParameter> result = new HashSet<RequestParameter>();

        if (externalContext == null || //detection of early config for different mojarra versions
                externalContext.getRequestParameterValuesMap() == null || externalContext.getRequest() == null)
        {
            return result;
        }

        NavigationParameterContext navigationParameterContext =
                BeanProvider.getContextualReference(NavigationParameterContext.class);

        for (Map.Entry<String, String> entry : navigationParameterContext.getPageParameters().entrySet())
        {
            //TODO add multi-value support
            result.add(new RequestParameter(entry.getKey(), new String[]{entry.getValue()}));
        }

        return result;
    }

    /**
     * Adds the current page-parameters to the given url
     *
     * @param externalContext current external-context
     * @param url             current url
     * @param encodeValues    flag which indicates if parameter values should be encoded or not
     * @return url with request-parameters
     */
    public static String addPageParameters(ExternalContext externalContext, String url, boolean encodeValues)
    {
        StringBuilder finalUrl = new StringBuilder(url);
        boolean existingParameters = url.contains("?");

        for (RequestParameter requestParam : getViewConfigPageParameters())
        {
            String key = requestParam.getKey();

            for (String parameterValue : requestParam.getValues())
            {
                if (!url.contains(key + "=" + parameterValue) &&
                        !url.contains(key + "=" + encodeURLParameterValue(parameterValue, externalContext)))
                {
                    if (!existingParameters)
                    {
                        finalUrl.append("?");
                        existingParameters = true;
                    }
                    else
                    {
                        finalUrl.append("&");
                    }

                    appendUrlParameter(finalUrl, key, parameterValue, encodeValues, externalContext);
                }
            }
        }
        return finalUrl.toString();
    }

    /**
     * Adds a paramter to the given url.
     *
     * @param externalContext   current external-context
     * @param url               current url
     * @param encodeValues      flag which indicates if parameter values should be encoded or not
     * @param name              the paramter name
     * @param value             the paramter value
     * @return url with appended parameter
     */
    public static String addParameter(ExternalContext externalContext, String url, boolean encodeValues,
            String name, String value)
    {
        // don't append if already available
        if (url.contains(name + "=" + value)
                || url.contains(name + "=" + encodeURLParameterValue(value, externalContext)))
        {
            return url;
        }

        StringBuilder finalUrl = SharedStringBuilder.get(SB_ADD_PARAMETER);
        finalUrl.append(url);

        if (url.contains("?"))
        {
            finalUrl.append("&");
        }
        else
        {
            finalUrl.append("?");
        }

        appendUrlParameter(finalUrl, name, value, encodeValues, externalContext);

        return finalUrl.toString();
    }

    /**
     * Adds the current request-parameters to the given url
     *
     * @param externalContext current external-context
     * @param url             current url
     * @param encodeValues    flag which indicates if parameter values should be encoded or not
     * @return url with request-parameters
     */
    public static String addRequestParameters(ExternalContext externalContext, String url, boolean encodeValues)
    {
        if (externalContext.getRequestParameterValuesMap().isEmpty())
        {
            return url;
        }

        StringBuilder finalUrl = new StringBuilder(url);
        boolean existingParameters = url.contains("?");

        for (Map.Entry<String, String[]> entry : externalContext.getRequestParameterValuesMap().entrySet())
        {
            for (String value : entry.getValue())
            {
                if (!url.contains(entry.getKey() + "=" + value) &&
                        !url.contains(entry.getKey() + "=" + encodeURLParameterValue(value, externalContext)))
                {
                    if (StringUtils.isEmpty(entry.getKey()) && StringUtils.isEmpty(value))
                    {
                        continue;
                    }

                    if (!existingParameters)
                    {
                        finalUrl.append("?");
                        existingParameters = true;
                    }
                    else
                    {
                        finalUrl.append("&");
                    }

                    appendUrlParameter(finalUrl, entry.getKey(), value, encodeValues, externalContext);
                }
            }
        }

        return finalUrl.toString();
    }

    protected static void appendUrlParameter(StringBuilder url, String name, String value, boolean encode,
            ExternalContext externalContext)
    {
        if (encode)
        {
            url.append(encodeURLParameterValue(name, externalContext));
        }
        else
        {
            url.append(name);
        }

        url.append("=");

        if (encode)
        {
            url.append(encodeURLParameterValue(value, externalContext));
        }
        else
        {
            url.append(value);
        }
    }

    /**
     * Encodes the given value using URLEncoder.encode() with the charset returned
     * from ExternalContext.getResponseCharacterEncoding().
     * This is exactly how the ExternalContext impl encodes URL parameter values.
     *
     * @param value           value which should be encoded
     * @param externalContext current external-context
     * @return encoded value
     */
    public static String encodeURLParameterValue(String value, ExternalContext externalContext)
    {
        // copied from MyFaces ServletExternalContextImpl.encodeURL()
        try
        {
            return URLEncoder.encode(value, externalContext.getResponseCharacterEncoding());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new UnsupportedOperationException("Encoding type="
                    + externalContext.getResponseCharacterEncoding() + " not supported", e);
        }
    }

    public static ViewConfigResolver getViewConfigResolver()
    {
        return BeanProvider.getContextualReference(ViewConfigResolver.class);
    }

    public static void saveFacesMessages(ExternalContext externalContext)
    {
        JsfModuleConfig jsfModuleConfig = BeanProvider.getContextualReference(JsfModuleConfig.class);

        if (!jsfModuleConfig.isAlwaysKeepMessages())
        {
            return;
        }

        try
        {
            WindowMetaData windowMetaData = BeanProvider.getContextualReference(WindowMetaData.class);

            Map<String, Object> requestMap = externalContext.getRequestMap();

            @SuppressWarnings({ "unchecked" })
            List<FacesMessageEntry> facesMessageEntryList =
                    (List<FacesMessageEntry>)requestMap.get(FacesMessageEntry.class.getName());

            if (facesMessageEntryList == null)
            {
                facesMessageEntryList = new CopyOnWriteArrayList<FacesMessageEntry>();
            }
            windowMetaData.setFacesMessageEntryList(facesMessageEntryList);
        }
        catch (ContextNotActiveException e)
        {
            //TODO log it in case of project-stage development
            //we can't handle it correctly -> delegate to the jsf-api (which has some restrictions esp. before v2.2)
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        }
    }

    public static void tryToRestoreMessages(FacesContext facesContext)
    {
        JsfModuleConfig jsfModuleConfig = BeanProvider.getContextualReference(JsfModuleConfig.class);

        if (!jsfModuleConfig.isAlwaysKeepMessages())
        {
            return;
        }

        try
        {
            WindowMetaData windowMetaData = BeanProvider.getContextualReference(WindowMetaData.class);

            @SuppressWarnings({ "unchecked" })
            List<FacesMessageEntry> facesMessageEntryList = windowMetaData.getFacesMessageEntryList();
            List<FacesMessage> originalMessageList = new ArrayList<FacesMessage>(facesContext.getMessageList());

            if (facesMessageEntryList != null)
            {
                for (FacesMessageEntry messageEntry : facesMessageEntryList)
                {
                    if (isNewMessage(originalMessageList, messageEntry.getFacesMessage()))
                    {
                        facesContext.addMessage(messageEntry.getComponentId(), messageEntry.getFacesMessage());
                    }
                }
                facesMessageEntryList.clear();
            }
        }
        catch (ContextNotActiveException e)
        {
            //TODO discuss how we handle it
        }
    }

    public static Throwable getRootCause(Throwable throwable)
    {
        while ((ELException.class.isInstance(throwable) || FacesException.class.isInstance(throwable) ||
                InvocationTargetException.class.isInstance(throwable)) && throwable.getCause() != null)
        {
            throwable = throwable.getCause();
        }

        return throwable;
    }

    public static boolean isNewMessage(List<FacesMessage> facesMessages, FacesMessage messageToCheck)
    {
        for (FacesMessage facesMessage : facesMessages)
        {
            if ((facesMessage.getSummary() != null && facesMessage.getSummary().equals(messageToCheck.getSummary()) ||
                    facesMessage.getSummary() == null && messageToCheck.getSummary() == null) &&
                    (facesMessage.getDetail() != null && facesMessage.getDetail().equals(messageToCheck.getDetail()) ||
                        facesMessage.getDetail() == null && messageToCheck.getDetail() == null))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if JSF 2.2+ is available and the delegation mode isn't deactivated via config, false otherwise
     */
    public static boolean isViewScopeDelegationEnabled()
    {
        return ClassUtils.tryToLoadClassForName("javax.faces.view.ViewScoped") != null &&
            JsfBaseConfig.ScopeCustomization.ViewDelegation.DELEGATE_TO_JSF;
    }

    public static void logWrongModuleUsage(String name)
    {
        Logger.getLogger(name).log(
            Level.WARNING, "You are using the JSF module for JSF 2.0/2.1 with JSF 2.2+ which " +
                "might cause issues in your application in different areas. Please upgrade " +
                    "org.apache.deltaspike.modules:deltaspike-jsf-module-impl-ee6 to " +
                        "org.apache.deltaspike.modules:deltaspike-jsf-module-impl");
    }

    public static void addStaticNavigationParameter(
        NavigationParameterContext navigationParameterContext, String key, String value)
    {
        Map<String, String> existingParameters = navigationParameterContext.getPageParameters();

        String existingValue = existingParameters.get(key);

        if (existingValue != null && value != null) //support null for special cases to reset an entry
        {
            return;
        }
        navigationParameterContext.addPageParameter(key, value);
    }

}
