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
import java.util.Arrays;

/**
 * Container for the exception and it's stack trace.
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Typed()
public final class ExceptionStackItem implements Serializable
{
    private static final long serialVersionUID = 5162936095781886477L;

    private final Throwable throwable;
    private final StackTraceElement[] stackTraceElements;

    public ExceptionStackItem(final Throwable cause)
    {
        this(cause, cause.getStackTrace());
    }

    public ExceptionStackItem(Throwable throwable, StackTraceElement[] stackTraceElements)
    {
        this.stackTraceElements = stackTraceElements.clone();
        this.throwable = throwable;
    }

    public StackTraceElement[] getStackTraceElements()
    {
        return stackTraceElements.clone();
    }

    public Throwable getThrowable()
    {
        return throwable;
    }

    @Override
    public String toString()
    {
        return new StringBuilder().
                append("throwable: ").append(throwable).append(", ").
                append("stackTraceElements: ").append(Arrays.toString(stackTraceElements)).
                toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ExceptionStackItem that = (ExceptionStackItem) o;

        if (!Arrays.equals(stackTraceElements, that.stackTraceElements))
        {
            return false;
        }
        if (!throwable.equals(that.throwable))
        {
            return false;
        }

        return true;

    }

    @Override
    public int hashCode()
    {
        int result = throwable.hashCode();
        result = 31 * result + Arrays.hashCode(stackTraceElements);
        return result;
    }
}
