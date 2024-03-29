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
package org.apache.deltaspike.test.jsf.impl.injection.uc004;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@FacesValidator(value = "myBeanValidator", managed = true)
public class MyBeanValidator implements Validator, StateHolder
{

    @Inject
    private MyBean myBean;

    private String validValue = "DeltaSpike";
    
    private boolean isTransient;

    public void setValidValue(String validValue)
    {
        this.validValue = validValue;
    }

    public String getValidValue()
    {
        return validValue;
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        if (value instanceof String)
        {
            String valueAString = (String) value;
            if (!myBean.isValid(valueAString, validValue))
            {
                throw new ValidatorException(new FacesMessage("The valid value should be " + validValue));
            }

        }
    }

    @Override
    public Object saveState(FacesContext context)
    {
        return this.validValue;
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        this.validValue = (String) state;
        
    }

    @Override
    public boolean isTransient()
    {
        return isTransient;
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
       this.isTransient = newTransientValue;
        
    }

}
