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
package org.apache.deltaspike.jsf.impl.listener.request;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;

import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextWrapper;
import java.io.IOException;
import javax.faces.context.FacesContext;
import org.apache.deltaspike.jsf.impl.scope.window.ClientWindowHelper;

public class DeltaSpikeExternalContextWrapper extends ExternalContextWrapper implements Deactivatable
{
    private final ExternalContext wrapped;

    DeltaSpikeExternalContextWrapper(ExternalContext wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public void redirect(String url) throws IOException
    {
        JsfUtils.saveFacesMessages(this.wrapped);
        this.wrapped.redirect(ClientWindowHelper.appendWindowId(FacesContext.getCurrentInstance(), url));
    }

    public ExternalContext getWrapped()
    {
        return wrapped;
    }
}
