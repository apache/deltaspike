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
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;

/**
 * Information about the current exception and exception cause container.  This object is not immutable.
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Typed()
public class ExceptionStack implements Serializable
{
    private static final long serialVersionUID = -6069790756478700680L;

    private boolean root;
    private boolean last;
    private int initialStackSize;
    private Throwable next;
    private Collection<ExceptionStackItem> remaining;
    private Deque<ExceptionStackItem> exceptionStackItems;
    // private Deque<ExceptionStackItem> origExceptionStackItems; // TODO: Later
    private Collection<Throwable> causes;
    private Throwable current;

    /**
     * Builds the stack from the given exception.
     *
     * @param exception Caught exception
     */
    public ExceptionStack(final Throwable exception)
    {
        if (exception == null)
        {
            throw new IllegalArgumentException("exception must not be null");
        }

        Throwable e = exception;
        this.exceptionStackItems = new ArrayDeque<ExceptionStackItem>();

        do
        {
            this.exceptionStackItems.addFirst(new ExceptionStackItem(e));
            if (e instanceof SQLException)
            {
                SQLException sqlException = (SQLException) e;

                while (sqlException.getNextException() != null)
                {
                    sqlException = sqlException.getNextException();
                    this.exceptionStackItems.addFirst(new ExceptionStackItem(sqlException));
                }
            }
            e = e.getCause();
        }
        while (e != null);

        this.initialStackSize = this.exceptionStackItems.size();
        this.causes = this.createThrowableCollection(exceptionStackItems);
        // TODO: Later this.origExceptionStackItems = new ArrayDeque<ExceptionStackItem>(exceptionStackItems);
        this.init();

    }

    private void init()
    {
        this.root = this.exceptionStackItems.size() == this.initialStackSize;

        if (!this.exceptionStackItems.isEmpty())
        {
            this.current = this.exceptionStackItems.removeFirst().getThrowable();
            this.remaining = Collections.unmodifiableCollection(this.exceptionStackItems);
        }
        else
        {
            this.remaining = Collections.emptyList();
            this.current = null;
        }

        this.last = this.remaining.isEmpty();
        this.next = (this.last) ? null : this.exceptionStackItems.peekFirst().getThrowable();
    }

    private Collection<ExceptionStackItem> createExceptionStackFrom(Collection<Throwable> throwables)
    {
        final Deque<ExceptionStackItem> returningCollection = new ArrayDeque<ExceptionStackItem>(throwables.size());

        for (Throwable t : throwables)
        {
            returningCollection.addFirst(new ExceptionStackItem(t));
        }

        return returningCollection;
    }

    private Collection<Throwable> createThrowableCollection(final Collection<ExceptionStackItem> exceptionStackItems)
    {
        // allow current
        final Deque<Throwable> returningCollection = new ArrayDeque<Throwable>(exceptionStackItems.size() + 1);

        for (ExceptionStackItem item : exceptionStackItems)
        {
            returningCollection.addFirst(item.getThrowable());
        }

        return returningCollection;
    }

    public Collection<Throwable> getCauseElements()
    {
        return Collections.unmodifiableCollection(this.causes);
    }

    /**
     * Test if iteration is finished
     *
     * @return finished with iteration
     */
    public boolean isLast()
    {
        return this.last;
    }

    public Throwable getNext()
    {
        return this.next;
    }

    public Collection<Throwable> getRemaining()
    {
        return Collections.unmodifiableCollection(this.createThrowableCollection(this.remaining));
    }

    /**
     * Tests if the current exception is the root exception
     *
     * @return Returns true if iteration is at the root exception (top of the inverted stack)
     */
    public boolean isRoot()
    {
        return this.root;
    }

    /**
     * Current exception in the iteration
     *
     * @return current exception
     */
    public Throwable getCurrent()
    {
        return this.current;
    }

    /**
     * Internal only.
     *
     * @param elements new stack.
     */
    public void setCauseElements(Collection<Throwable> elements)
    {
        this.exceptionStackItems = new ArrayDeque<ExceptionStackItem>(this.createExceptionStackFrom(elements));
        this.init();
    }

    /**
     * Internal only.
     */
    public void skipCause()
    {
        this.init();
    }

    /**
     * Done later
     * The original exception stack if it has been changed.
     *
     * @return The original exception stack

    public Deque<ExceptionStackItem> getOrigExceptionStackItems() {
    }
     */
}
