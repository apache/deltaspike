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
package org.apache.deltaspike.data.spi;

/**
 * A marker interface. Used for writing custom query methods:
 * <pre>
 * public interface RepositoryExtension<E> {
 *     E saveAndFlushAndRefresh(E entity);
 * }
 *
 * public class DelegateRepositoryExtension<E> implements RepositoryExtension<E>, DelegateQueryHandler {
 *    &#064;Inject
 *    private QueryInvocationContext context;
 *
 *    &#064;Override
 *    public E saveAndFlushAndRefresh(E entity) {
 *        ...
 *    }
 * }
 * </pre>
 *
 * The extension is now usable with:
 * <pre>
 * &#064;Repository
 * public interface MySimpleRepository
 *         extends RepositoryExtension<Simple>, EntityRepository<Simple, Long> {
 * }
 * </pre>
 */
public interface DelegateQueryHandler
{
}
