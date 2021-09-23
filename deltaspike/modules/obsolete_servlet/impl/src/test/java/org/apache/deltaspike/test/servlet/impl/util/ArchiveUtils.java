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
package org.apache.deltaspike.test.servlet.impl.util;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.utils.ShrinkWrapArchiveUtil;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains helpers for building frequently used archives
 */
public class ArchiveUtils
{
    public static JavaArchive[] getDeltaSpikeCoreAndServletModuleArchive()
    {
        JavaArchive extensionsJar = ShrinkWrap
                .create(JavaArchive.class, "dsCoreTest.jar")
                .addClass(ArchiveUtils.class)
                .addPackages(true, "org.apache.http")
                .addPackages(true, "org.jboss.shrinkwrap.api") //TODO needed by the setup for tomee -> re-visit it
                .addPackage(WebProfileCategory.class.getPackage());

        JavaArchive[] coreArchives = ShrinkWrapArchiveUtil.getArchives(null,
                "META-INF/beans.xml",
                new String[]{"org.apache.deltaspike.core", "org.apache.deltaspike.test.category",
                        "org.apache.deltaspike.servlet"}, null,
                "ds-core_and_servlet");

        List<JavaArchive> archives = new ArrayList<JavaArchive>(Arrays.asList(coreArchives));
        archives.add(extensionsJar);
        return archives.toArray(new JavaArchive[archives.size()]);
    }
}
