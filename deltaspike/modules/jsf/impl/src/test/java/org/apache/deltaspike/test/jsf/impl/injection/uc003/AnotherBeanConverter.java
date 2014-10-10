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
package org.apache.deltaspike.test.jsf.impl.injection.uc003;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@FacesConverter("myValueConveter")
public class AnotherBeanConverter extends AbstractStateHolder implements Converter
{

    @Inject
    private AnotherBean myValue;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String value)
    {
        try
        {
            if (value == null || value.isEmpty())
            {
                return null;
            }
            Integer.parseInt(value);
            return myValue.getAsObject(value);
        }
        catch (NumberFormatException e)
        {
            throw new ConverterException(new FacesMessage("Value is not an Integer"));
        }

    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object value)
    {
        return myValue.getAsString(value);
    }

}
