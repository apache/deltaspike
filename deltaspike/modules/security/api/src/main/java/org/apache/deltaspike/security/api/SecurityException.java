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

package org.apache.deltaspike.security.api;

/**
 * Any exception that is raised by the security module extends from this runtime
 * exception class, making it easy for other modules and extensions to catch all
 * security-related exceptions in a single catch block, if need be.
 */
public abstract class SecurityException extends RuntimeException 
{
    private static final long serialVersionUID = 789326682407249952L;

    public SecurityException() 
    {
        super();
    }

    public SecurityException(String message, Throwable cause) 
    {
        super(message, cause);
    }

    public SecurityException(String message) 
    {
        super(message);
    }

    public SecurityException(Throwable cause) 
    {
        super(cause);
    }
}
