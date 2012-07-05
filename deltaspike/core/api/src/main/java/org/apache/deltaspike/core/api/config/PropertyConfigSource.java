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
package org.apache.deltaspike.core.api.config;

/**
 *  <p>If you implement this interface inside a Bean Archive
 *  (a JAR or ClassPath entry with a META-INF/beans.xml file),
 *  the property files with the given file name
 *  will be registered as {@link org.apache.deltaspike.core.spi.config.ConfigSource}s.</p>
 *
 *  <p>DeltaSpike will automatically pickup all the implementations
 *  during the {@link javax.enterprise.inject.spi.ProcessAnnotatedType}
 *  phase and create a new instance via reflection. Thus the
 *  implementations will need a non-private default constructor.
 *  There is <b>no</b> CDI injection being performed in those instances!
 *  The scope of the implementations will also be ignored as they will
 *  not get picked up as CDI beans.</p>
 *
 *  <p>Please note that the configuration will only be available
 *  after the boot is finished. This means that you cannot use
 *  this configuration inside a CDI Extension before the boot
 *  is finished!</p>
 */
public interface PropertyConfigSource extends DeltaSpikeConfig
{
    /**
     * All the property files on the classpath which have this
     * name will get picked up and registered as {@link org.apache.deltaspike.core.spi.config.ConfigSource}s.
     *
     * @return the full path name of the property files to pick up.
     */
    String getPropertyFileName();
}
