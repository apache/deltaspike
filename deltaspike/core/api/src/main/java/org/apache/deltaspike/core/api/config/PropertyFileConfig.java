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
 * <p>
 * If you implement this interface, the property files with the given file name will be registered as
 * {@link org.apache.deltaspike.core.spi.config.ConfigSource}s.</p>
 *<p>There are 2 ways to register a {@code PropertyFileConfig}</p>
 *
 * <h3>1. Automatic pickup via {@code ProcessAnnotatedType} phase</h3>
 * <p>
 * DeltaSpike will automatically pick up all the implementations which are
 * inside a Bean Archive (a JAR or ClassPath entry with a META-INF/beans.xml file) during the
 * {@link javax.enterprise.inject.spi.ProcessAnnotatedType} phase and create a new instance via reflection. Thus the
 * implementations will need a non-private default constructor. There is <b>no</b> CDI injection being performed in
 * those instances! The scope of the implementations will also be ignored as they will not get picked up as CDI
 * beans.</p>
 *
 * <p>
 * Please note that the configuration will only be available after the boot is finished. This means that you cannot use
 * this configuration inside a CDI Extension before the boot is finished!</p>
 *
 * <p><b>Attention:</b> When using this logic inside an EAR then you might get
 * different behaviour depending on the Java EE
 * server you are using. Some EE container use a different ClassLoader to bootstrap
 * the application than later to serve Requests.
 * In that case we would register the ConfigSources on the <em>wrong</em> ConfigResolver
 * (means we register it to the wrong ClassLoader). If you did hit such an application server
 * then you might need to switch back to manually register the
 * {@link org.apache.deltaspike.core.spi.config.ConfigSource} or
 * {@link org.apache.deltaspike.core.spi.config.ConfigSourceProvider} via the
 * {@link java.util.ServiceLoader} mechanism described there.</p>.
 *
 * <h3>2. Automatic pickup via {@code java.util.ServiceLoader} mechanism</h3>
 * <p>In case you have an EAR or you need the configured values already during the CDI container start
 * then you can also register the PropertyFileConfig via the {@code java.util.ServiceLoader} mechanism.
 * To not have this configuration picked up twice it is required to annotate your own
 * {@code PropertyFileConfig} implementation with {@link org.apache.deltaspike.core.api.exclude.Exclude}.</p>
 *
 * <p>The {@code ServiceLoader} mechanism requires to have a file
 * <pre>
 *     META-INF/services/org.apache.deltaspike.core.api.config.PropertyFileConfig
 * </pre>
 * containing the fully qualified Class name of your own {@code PropertyFileConfig} implementation class.
 * <pre>
 *     com.acme.my.own.SomeSpecialPropertyFileConfig
 * </pre>
 * The implementation will look like the following:
 * <pre>
 *     &#064;Exclude
 *     public class SomeSpecialPropertyFileConfig implements PropertyFileConfig {
 *         public String getPropertyFileName() {
 *             return "myconfig/specialconfig.properties"
 *         }
 *         public boolean isOptional() {
 *             return false;
 *         }
 *     }
 * </pre>
 * </p>
 *
 */
public interface PropertyFileConfig
{
    /**
     * All the property files on the classpath which have this name will get picked up and registered as
     * {@link org.apache.deltaspike.core.spi.config.ConfigSource}.
     *
     * If the the returned String starts with 'file://' then we pick up the configuration from a file
     * on the File System instead of the ClassPath.
     * The same works for other URLs which are passed, e.g. 'http://'.
     * Note that reading the property values only gets performed once right now.
     *
     * @return the full file name (including path) of the property files to pick up.
     */
    String getPropertyFileName();

    /**
     * @return true if the file is optional, false if the specified file has to be in place.
     */
    boolean isOptional();
}
