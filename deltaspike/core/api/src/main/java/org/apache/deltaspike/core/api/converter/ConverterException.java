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
package org.apache.deltaspike.core.api.converter;

//TODO TBD

/**
 * Exception which provides the source- and target-type as payload
 */
public class ConverterException extends RuntimeException
{
    private static final long serialVersionUID = -1399119195483111935L;

    private Class<?> sourceType;
    private Class<?> targetType;

    public ConverterException(Class<?> sourceType, Class<?> targetType, Throwable cause)
    {
        super(cause);
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    public Class<?> getSourceType()
    {
        return sourceType;
    }

    public Class<?> getTargetType()
    {
        return targetType;
    }
}
