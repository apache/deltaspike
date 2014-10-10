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

import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;

public abstract class AbstractStateHolder implements StateHolder
{

    private boolean isTransient;

    @Override
    public Object saveState(FacesContext context)
    {
        // no need to really save the state
        return null;
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        // no need to really restore the state
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
