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

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.deltaspike.core.util.ClassUtils;

/**
 * This class holds the {@link ServletContext} for each context class loader.
 */
class ServletContextHolder
{

    private static final Logger log = Logger.getLogger(ServletContextHolder.class.getName());

    private static final Map<ClassLoader, ServletContext> CONTEXT_BY_CLASSLOADER = Collections.synchronizedMap(
            new WeakHashMap<ClassLoader, ServletContext>());

    private ServletContextHolder()
    {
        // hide constructor
    }

    /**
     * Bind the supplied {@link ServletContext} to the current context class loader. Subsequent calls to {@link #get()}
     * with the same context class loader will always return this context.
     * 
     * @param servletContext
     *            The context to bind to the context class loader
     */
    static void bind(ServletContext servletContext)
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);
        ServletContext existingContext = CONTEXT_BY_CLASSLOADER.put(classLoader, servletContext);
        if (existingContext != null)
        {
            throw new IllegalArgumentException("There is already a ServletContext associated with class loader: "
                    + classLoader);
        }
    }

    /**
     * Returns the {@link ServletContext} associated with the current context class loader.
     * 
     * @throws IllegalStateException
     *             if there is no {@link ServletContext} stored for the current context class loader
     */
    static ServletContext get()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);
        ServletContext servletContext = CONTEXT_BY_CLASSLOADER.get(classLoader);
        if (servletContext == null)
        {
            throw new IllegalStateException("There is no ServletContext stored for class loader: " + classLoader);
        }
        return servletContext;
    }

    /**
     * Releases the {@link ServletContext} from the current context class loader. Subsequent calls to {@link #get()}
     * with the same context class loader will return <code>null</code>.
     */
    static void release()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);
        ServletContext removedContext = CONTEXT_BY_CLASSLOADER.remove(classLoader);
        if (removedContext == null)
        {
            log.warning("Cannot find a ServletContext to release for class loader: " + classLoader);
        }
    }

}
