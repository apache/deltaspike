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
package org.apache.deltaspike.jsf.impl.injection;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;

import javax.faces.FacesWrapper;
import javax.faces.component.PartialStateHolder;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;

//no backward compatibility for full state-saving
abstract class AbstractContextualReferenceWrapper<T> implements PartialStateHolder, FacesWrapper<T>
{
    private T wrapped;
    private transient Boolean fullStateSavingFallbackEnabled;

    protected AbstractContextualReferenceWrapper()
    {
    }

    protected AbstractContextualReferenceWrapper(T wrapped, boolean fullStateSavingFallbackEnabled)
    {
        this.wrapped = wrapped;
        this.fullStateSavingFallbackEnabled = fullStateSavingFallbackEnabled;
    }

    @Override
    public void markInitialState()
    {
        if (this.wrapped instanceof PartialStateHolder)
        {
            ((PartialStateHolder) this.wrapped).markInitialState();
        }
    }

    @Override
    public void clearInitialState()
    {
        if (this.wrapped instanceof PartialStateHolder)
        {
            ((PartialStateHolder) this.wrapped).clearInitialState();
        }
    }

    @Override
    public boolean initialStateMarked()
    {
        return this.wrapped instanceof PartialStateHolder && ((PartialStateHolder) this.wrapped).initialStateMarked();
    }

    @Override
    public Object saveState(FacesContext context)
    {
        if (this.fullStateSavingFallbackEnabled == null)
        {
            this.fullStateSavingFallbackEnabled =
                BeanProvider.getContextualReference(JsfModuleConfig.class).isFullStateSavingFallbackEnabled();
        }

        if (this.wrapped instanceof StateHolder)
        {
            Object[] result = new Object[2];

            if (this.fullStateSavingFallbackEnabled)
            {
                result[0] = this.getWrapped().getClass().getName();
            }
            result[1] = ((StateHolder)wrapped).saveState(context);
            return result;
        }

        if (this.fullStateSavingFallbackEnabled)
        {
            Object[] result = new Object[1];
            result[0] = this.getWrapped().getClass().getName();

            return result;
        }
        return null;
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        Object[] wrappedState = (Object[]) state;

        if (this.wrapped == null) //fallback for full state-saving
        {
            Class wrappedClass = ClassUtils.tryToLoadClassForName((String)wrappedState[0]);

            T resolvedInstance = resolveInstanceForClass(context, wrappedClass);

            //TODO re-visit logic for multiple (custom) wrappers
            if (resolvedInstance instanceof AbstractContextualReferenceWrapper)
            {
                resolvedInstance = ((AbstractContextualReferenceWrapper<T>)resolvedInstance).getWrapped();
            }
            this.wrapped = resolvedInstance;
        }

        if (this.wrapped == null)
        {
            this.wrapped = (T)ClassUtils.tryToInstantiateClassForName((String)wrappedState[0]);
            BeanProvider.injectFields(this.wrapped);
        }

        if (this.wrapped instanceof StateHolder)
        {
            ((StateHolder) this.wrapped).restoreState(context, wrappedState[1]);
        }
    }

    @Override
    public boolean isTransient()
    {
        return this.wrapped instanceof StateHolder && ((StateHolder) this.wrapped).isTransient();
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
        if (this.wrapped instanceof StateHolder)
        {
            ((StateHolder) this.wrapped).setTransient(newTransientValue);
        }
    }

    @Override
    public T getWrapped()
    {
        return this.wrapped;
    }

    protected abstract T resolveInstanceForClass(FacesContext facesContext, Class<?> wrappedClass);
}
