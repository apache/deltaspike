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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class JsfUtils
{
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
     * Adds the current request-parameters to the given url
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
                    finalUrl.append(key);
                    finalUrl.append("=");

                    if (encodeValues)
                    {
                        finalUrl.append(JsfUtils.encodeURLParameterValue(parameterValue, externalContext));
                    }
                    else
                    {
                        finalUrl.append(parameterValue);
                    }
                }
            }
        }
        return finalUrl.toString();
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
}
