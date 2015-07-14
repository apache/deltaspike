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
package org.apache.deltaspike.servlet.impl.produce;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * This class stores the ServletRequest in the {@link RequestResponseHolder}.
 */
public class RequestResponseHolderListener implements ServletRequestListener, Deactivatable
{

    private final boolean activated;

    public RequestResponseHolderListener()
    {
        this.activated = ClassDeactivationUtils.isActivated(this.getClass());
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre)
    {
        if (activated)
        {
            /*
             * For some reason Tomcat seems to call requestInitialized() more than
             * once for a request. Not sure if this allowed according to the spec.
             */
            if (!RequestResponseHolder.REQUEST.isBound())
            {
                RequestResponseHolder.REQUEST.bind(sre.getServletRequest());
            }
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre)
    {
        if (activated)
        {
            RequestResponseHolder.REQUEST.release();
        }
    }

}
