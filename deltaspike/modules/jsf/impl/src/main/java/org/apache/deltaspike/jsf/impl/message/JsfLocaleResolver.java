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
package org.apache.deltaspike.jsf.impl.message;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import java.util.Locale;

import org.apache.deltaspike.core.impl.message.DefaultLocaleResolver;

/**
 * A LocaleResolver which evaluates the FacesContext.
 */
@ApplicationScoped
@Specializes
public class JsfLocaleResolver extends DefaultLocaleResolver
{
    @Override
    public Locale getLocale()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null)
        {
            UIViewRoot viewRoot = facesContext.getViewRoot();
            if (viewRoot != null)
            {
                // if a ViewRoot is present we return the Locale from there
                return viewRoot.getLocale();
            }
        }

        // if no FacesContext is present we return the default Locale
        return super.getLocale();
    }
}
