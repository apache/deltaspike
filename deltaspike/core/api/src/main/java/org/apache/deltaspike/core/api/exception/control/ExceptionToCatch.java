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

package org.apache.deltaspike.core.api.exception.control;

import javax.enterprise.inject.Typed;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Entry point event into the Catch system.  This object is nearly immutable, the only mutable portion
 * is the handled flag.
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Typed()
public class ExceptionToCatch implements Serializable
{
    private static final long serialVersionUID = 2646115104528108266L;

    private Throwable exception;
    private boolean handled;
    private transient Set<Annotation> qualifiers;

    /**
     * Constructor that adds qualifiers for the handler(s) to run.
     * Typically only integrators will be using this constructor.
     *
     * @param exception  Exception to handle
     * @param qualifiers qualifiers to use to narrow the handlers called
     */
    public ExceptionToCatch(Throwable exception, Annotation... qualifiers)
    {
        this.exception = exception;
        this.qualifiers = new HashSet<Annotation>();
        Collections.addAll(this.qualifiers, qualifiers);
    }

    /**
     * Basic constructor without any qualifiers defined.
     *
     * @param exception Exception to handle.
     */
    public ExceptionToCatch(Throwable exception)
    {
        this.exception = exception;
        this.qualifiers = Collections.emptySet();
    }

    public Throwable getException()
    {
        return this.exception;
    }

    /**
     * Internal only.
     *
     * @param handled new value
     */
    public void setHandled(boolean handled)
    {
        this.handled = handled;
    }

    /**
     * Test to see if the exception has been handled via Solder Catch.
     *
     * @return test if the exception has been through Solder Catch handling.
     */
    public boolean isHandled()
    {
        return this.handled;
    }

    /**
     * Qualifiers with which the instance was created.
     *
     * @return Qualifiers with which the instance was created.
     */
    public Set<Annotation> getQualifiers()
    {
        return Collections.unmodifiableSet(this.qualifiers);
    }
}
