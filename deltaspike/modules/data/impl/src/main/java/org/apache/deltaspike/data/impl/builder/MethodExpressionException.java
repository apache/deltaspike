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
package org.apache.deltaspike.data.impl.builder;

public class MethodExpressionException extends RuntimeException
{

    private static final long serialVersionUID = 1L;
    private final String property;
    private final Class<?> repoClass;
    private final String method;

    public MethodExpressionException(Class<?> repoClass, String method)
    {
        this(null, repoClass, method);
    }

    public MethodExpressionException(String property, Class<?> repoClass, String method)
    {
        this.property = property;
        this.repoClass = repoClass;
        this.method = method;
    }

    @Override
    public String getMessage()
    {
        if (property != null)
        {
            return "Invalid property '" + property + "' in method expression " + repoClass.getName() + "." + method;
        }
        return "Method '" + method + "'of Repository " + repoClass.getName() + " is not a method expression";
    }

}
