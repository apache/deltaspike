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
package org.apache.deltaspike.core.impl.interceptor.interdyn;


import java.lang.annotation.Annotation;

/**
 * Contains a mapping between a dynamic interceptor rule and the name of the additional annotation to be added
 */

public class AnnotationRule
{
    /**
     * A RegExp to identify the classes which should get modified
     */
    private String rule;

    /**
     * The Annotation to be added
     */
    private Annotation additionalAnnotation;

    private boolean requiresProxy;

    public AnnotationRule(String rule, Annotation interceptorBinding, boolean requiresProxy)
    {
        this.rule = rule;
        this.additionalAnnotation = interceptorBinding;
        this.requiresProxy = requiresProxy;
    }

    public String getRule()
    {
        return rule;
    }

    public Annotation getAdditionalAnnotation()
    {
        return additionalAnnotation;
    }

    public boolean requiresProxy()
    {
        return requiresProxy;
    }
}
