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
package org.apache.deltaspike.testcontrol.impl.mock;

import org.apache.deltaspike.testcontrol.spi.mock.MockFilter;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;

public class DefaultMockFilter implements MockFilter
{
    private static final String OWB_BASE_PACKAGE = "org.apache.webbeans.";
    private static final String WELD_BASE_PACKAGE = "org.jboss.weld.";

    @Override
    public boolean isMockedImplementationSupported(Annotated annotated)
    {
        String packageName = null;

        if (annotated instanceof AnnotatedType)
        {
            packageName = ((AnnotatedType)annotated).getJavaClass().getPackage().getName();
        }
        else if (annotated instanceof AnnotatedMember)
        {
            packageName = ((AnnotatedMember)annotated).getJavaMember().getDeclaringClass().getPackage().getName();
        }

        return packageName != null && !isInternalPackage(packageName);
    }

    protected boolean isInternalPackage(String packageName)
    {
        return packageName.startsWith(OWB_BASE_PACKAGE) || packageName.startsWith(WELD_BASE_PACKAGE);
    }
}
