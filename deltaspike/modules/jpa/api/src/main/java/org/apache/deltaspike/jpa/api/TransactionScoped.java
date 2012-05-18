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
package org.apache.deltaspike.jpa.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.context.NormalScope;

/**
 * <p>A &#064;TransactionScoped contextual instance will unique for a given
 * CODI-managed Transaction. The context will get started when the outermost
 * {@link Transactional} method gets invoked and will get closed when
 * the call chain leaves the outermost {@link Transactional} method.</p>
 *
 * <p>The classic use-case is for producing JPA EntityManagers.
 * <pre>
 *  &#064;Dependent
 *  public class EntityManagerProducer
 *  {
 *      private &#064;PersistenceContext(unitName = "test") EntityManager entityManager;
 *
 *      public &#064;Produces &#064;TransactionScoped EntityManager createEntityManager()
 *      {
 *          return entityManager;
 *      }
 *
 *      public void closeEntityManager(&#064;Disposes EntityManager em)
 *      {
 *          em.close();
 *      }
 *  }
 * </pre>
 * </p>
 *
 *
 * @see Transactional
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@NormalScope(passivating = false)
public @interface TransactionScoped
{
}
