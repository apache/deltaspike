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
package org.apache.deltaspike.cdise.api;


import javax.enterprise.inject.spi.BeanManager;
import java.util.Map;


/**
 * <p>A CdiTestContainer provides access to an underlying JSR-299 (CDI)
 * Container. It allows starting and stopping the container and to start
 * and stop the built-in contexts of that container.</p>
 *
 * <p>The intention is to provide a portable control for CDI containers in
 * Java SE environments. It is <b>not</b> intended for environments in which the
 * CDI container is under full control of the server already, e.g. in
 * EE-containers.</p>
 */
public interface CdiContainer
{
    /**
     * <p>Booting the CdiTestContainer will scan the whole classpath
     * for Beans and extensions available.
     * The container might throw a DeploymentException or similar on startup.</p>
     *
     * <p><b>Note:</b> booting the container does <i>not</i> automatically
     * start all CDI Contexts! Depending on the underlying CDI container you
     * might need to invoke {@link #getContextControl()} and execute
     * {@link ContextControl#startContext(Class)} or
     * {@link ContextControl#startContexts()}</p>
     */
    void boot();

    /**
     * <p>Like {@link #boot()} but allows to pass in a configuration Map
     * for the container.</p>
     * <p>Please note that the configuration is container implementation dependent!</p>
     *
     * @param properties
     */
    void boot(Map<?,?> properties);
    
    /**
     * This will shutdown the underlying CDI container and stop all contexts.
     */
    void shutdown();
    

    /**
     * @return the {@link BeanManager} or <code>null</code> it not available
     */
    BeanManager getBeanManager();

    /**
     * @return ContextControl for the started Container. <code>null</code> if the container is not yet started
     */
    ContextControl getContextControl();

}
