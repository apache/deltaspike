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

import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.test.utils.CdiContainerUnderTest;
import org.apache.deltaspike.test.utils.ShrinkWrapArchiveUtil;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * This class contains helpers for building frequently used archives
 */
public class ArchiveUtils
{
    private ArchiveUtils()
    {
    }

    public static JavaArchive[] getDeltaSpikeCoreAndSchedulerArchive()
    {
        return ShrinkWrapArchiveUtil.getArchives(
                null,
                "META-INF/beans.xml",
                new String[]{"org.apache.deltaspike.core",
                        "org.apache.deltaspike.test.category",
                        "org.apache.deltaspike.scheduler"},
                null,
                "ds-core_and_scheduler");
    }

    public static JavaArchive getContextControlForDeployment()
    {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "cdi-control.jar")
                .addClass(ContextControl.class);

        if (CdiContainerUnderTest.is("owb"))
        {
            jar.addPackage("org.apache.deltaspike.cdise.owb");
        }
        return jar;
    }
}
