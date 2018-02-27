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
package org.apache.deltaspike.core.spi.alternative;

import org.apache.deltaspike.core.spi.activation.Deactivatable;

import java.util.Map;

public interface AlternativeBeanClassProvider extends Deactivatable
{
    /**
     * @return mapping between the interface of the bean an the alternative-bean-class
     */
    //instead of Class/Class we are using String/String to avoid classloader issues e.g. with EARs
    Map<String, String> getAlternativeMapping();
}
