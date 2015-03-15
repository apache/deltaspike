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
package org.apache.deltaspike.jsf.impl.injection.proxy;

import javax.faces.component.PartialStateHolder;
import javax.faces.context.FacesContext;

//the converter-/validator proxy needs to implement PartialStateHolder to force a special path of the jsf state handling
//which forces a call to InjectionAwareApplicationWrapper on the postback.
//this class provides the default behaviour for the reflection calls,
//if the original converter-/validator doesn't implement the interface
public class DefaultPartialStateHolder implements PartialStateHolder
{
    private boolean transientValue;
    private boolean initialStateMarked;

    @Override
    public Object saveState(FacesContext context)
    {
        return null; //not needed
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        //not needed
    }

    @Override
    public boolean isTransient()
    {
        return this.transientValue;
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
        this.transientValue = newTransientValue;
    }

    @Override
    public void clearInitialState()
    {
        this.initialStateMarked = false;
    }

    @Override
    public boolean initialStateMarked()
    {
        return this.initialStateMarked;
    }

    @Override
    public void markInitialState()
    {
        this.initialStateMarked = true;
    }
}
