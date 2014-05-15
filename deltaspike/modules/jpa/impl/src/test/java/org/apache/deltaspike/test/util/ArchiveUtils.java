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
package org.apache.deltaspike.test.util;

import org.apache.deltaspike.test.jpa.api.shared.TestEntityManager;
import org.apache.deltaspike.test.utils.ShrinkWrapArchiveUtil;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * This class contains helpers for building frequently used archives
 */
public class ArchiveUtils
{
    public static final String SHARED_PACKAGE = TestEntityManager.class.getPackage().getName();

    private ArchiveUtils()
    {
        // private ct
    }

    public static JavaArchive[] getDeltaSpikeCoreAndJpaArchive()
    {
        return ShrinkWrapArchiveUtil.getArchives(
                null,
                "META-INF/beans.xml",
                new String[]{"org.apache.deltaspike.core", "org.apache.deltaspike.jpa"},
                null,
                "ds-core_and_jpa");
    }

    public static Asset getBeansXml()
    {
        @SuppressWarnings("UnnecessaryLocalVariable")
        Asset beansXml = new StringAsset(
            "<beans>" +
                "<interceptors>" +
                    "<class>org.apache.deltaspike.jpa.impl.transaction.TransactionalInterceptor</class>" +
                "</interceptors>" +
            "</beans>"
        );

        return beansXml;
    }
}
