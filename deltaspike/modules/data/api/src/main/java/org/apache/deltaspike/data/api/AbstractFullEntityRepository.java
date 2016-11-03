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
package org.apache.deltaspike.data.api;

import java.io.Serializable;

/**
 * Full repository base class to be extended by concrete implementations. A convenience class
 * combining {@code AbstractEntityRepository}, {@code EntityManagerDelegate} and
 * {@code CriteriaSupport}.
 * 
 * @param <E>
 *            Entity type.
 * @param <PK>
 *            Primary key type.
 */
public abstract class AbstractFullEntityRepository<E, PK extends Serializable> extends
    AbstractEntityRepository<E, PK> implements FullEntityRepository<E, PK>
{
}
