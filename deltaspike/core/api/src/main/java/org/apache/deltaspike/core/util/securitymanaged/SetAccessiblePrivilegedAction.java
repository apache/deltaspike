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

package org.apache.deltaspike.core.util.securitymanaged;

import javax.enterprise.inject.Typed;
import java.lang.reflect.AccessibleObject;
import java.security.PrivilegedAction;

/**
 * PrivilegedAction instance to enabling access to the specified {@link AccessibleObject}.
 * It's only useful if {@link System#getSecurityManager()} returns a {@link SecurityManager}.
 */
@Typed()
public class SetAccessiblePrivilegedAction implements PrivilegedAction<Void>
{
    private final AccessibleObject member;

    public SetAccessiblePrivilegedAction(AccessibleObject member)
    {
        this.member = member;
    }

    public Void run()
    {
        member.setAccessible(true);
        return null;
    }
}
