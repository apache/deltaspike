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
package org.apache.deltaspike.security.api.permission.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes an entity bean (i.e. a class annotated with @Entity) as being a storage container
 * for object permissions.  If the value member is set, then the annotated entity will be used
 * to lookup object permissions for objects of that class only, otherwise if it is not set the
 * entity will be used to store general object permissions (only one entity may be used for
 * general permissions, if more than one entity is defined then a deployment exception will be
 * thrown).
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface ACLStore
{
    Class<?> value() default GENERAL.class;
    
    // Dummy class to enable the entity bean for general storage of ACL permissions
    static final class GENERAL 
    { }
}
