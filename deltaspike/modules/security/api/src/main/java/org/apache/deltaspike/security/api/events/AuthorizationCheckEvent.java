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

package org.apache.deltaspike.security.api.events;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * This event may be used to perform an authorization check.  The constructor
 * should be provided with one or more annotation literal values representing the
 * security binding types to be checked.  After firing the event, the isPassed()
 * method should be used to determine whether the authorization check was
 * successful.
 * <p/>
 * WARNING - This event should only be fired and observed synchronously.
 * Unpredictable results may occur otherwise.
 */
public class AuthorizationCheckEvent 
{
    private boolean passed;
    
    private List<? extends Annotation> bindings;

    public AuthorizationCheckEvent(List<? extends Annotation> bindings) 
    {
        this.bindings = bindings;
    }

    public List<? extends Annotation> getBindings() 
    {
        return bindings;
    }

    public void setPassed(boolean value) 
    {
        this.passed = value;
    }

    public boolean isPassed() 
    {
        return passed;
    }
}
