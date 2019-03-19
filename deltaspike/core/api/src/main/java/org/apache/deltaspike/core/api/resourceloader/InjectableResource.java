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
package org.apache.deltaspike.core.api.resourceloader;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Qualifier which enables simple injection of resources into beans, eliminating the need to deal with their loading.
 *
 * <p>
 * <b>Example:</b>
 * <pre>
 * &#064;Inject
 * &#064;InjectableResource(location="myfile.properties")
 * private Properties props;
 *
 * &#064;Inject
 * &#064;InjectableResource(location="config.xml")
 * private InputStream inputStream;
 * </pre>
 *
 * This can be used to read files, from classpath or the file system, using two default implementations:
 * ClasspathResourceProvider and FileResourceProvider. They can be extended as well by implementing the
 * InjectableResourceProvider interface to allow reading from alternate sources, if needed (e.g. database LOBs, NoSQL
 * storage areas).
 * </p>
 */
@Target( { TYPE, METHOD, PARAMETER, FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface InjectableResource
{
    @Nonbinding
    Class<? extends InjectableResourceProvider> resourceProvider() default ClasspathResourceProvider.class;

    @Nonbinding
    String location() default "";
}
