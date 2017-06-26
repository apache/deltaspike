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
package org.apache.deltaspike.proxy.spi;

public interface DeltaSpikeProxyClassGenerator
{
    /**
     * Generates a proxy class from the given source class, which also implements {@link DeltaSpikeProxy}.
     * The proxy class will be generated in the same package as the original class
     * and the suffix will be appended to the name of the class.
     * 
     * @param <T> The target class.
     * @param classLoader The {@link ClassLoader} to be used to define the proxy class.
     * @param targetClass The class to proxy.
     * @param suffix The classname suffix.
     * @param superAccessorMethodSuffix It's required to generate methods which just invokes the original method.
     *                                  We generate them with the same name as the original method
     *                                  and append the suffix.
     * @param additionalInterfaces Additional interfaces which should be implemented.
     *                             Please note that you must also pass new methods via <c>delegateMethods</c>.
     * @param delegateMethods Methods which should be delegated to the
     *                        {@link DeltaSpikeProxy#getDelegateInvocationHandler()}
     *                        instead of invoking the original method.
     * @param interceptMethods Methods which should be intercepted (to call interceptors or decorators)
     *                         before invoking the original method.
     * @return The generated proxy class.
     */
    <T> Class<T> generateProxyClass(ClassLoader classLoader,
                                    Class<T> targetClass,
                                    String suffix,
                                    String superAccessorMethodSuffix,
                                    Class<?>[] additionalInterfaces,
                                    java.lang.reflect.Method[] delegateMethods,
                                    java.lang.reflect.Method[] interceptMethods);
}
