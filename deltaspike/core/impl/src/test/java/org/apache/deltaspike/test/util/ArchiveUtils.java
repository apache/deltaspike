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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.core.api.util.context.DummyContext;
import org.apache.deltaspike.test.core.api.util.context.DummyScopeExtension;
import org.apache.deltaspike.test.core.api.util.context.DummyScoped;
import org.apache.deltaspike.test.utils.ShrinkWrapArchiveUtil;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * This class contains helpers for building frequently used archives
 */
public class ArchiveUtils
{
    public static JavaArchive[] getDeltaSpikeCoreArchive()
    {
        return getDeltaSpikeCoreArchive(null);
    }

    public static JavaArchive[] getDeltaSpikeCoreArchive(String[] excludedPackagesOrFiles)
    {
        // we also need quite some internal Arquillian classes on the client side
        // this JAR has NO beans.xml to prevent class scanning!
        JavaArchive extensionsJar = ShrinkWrap
                .create(JavaArchive.class, "testExtensions.jar")
                .addClass(ArchiveUtils.class)
                .addClass(DummyScopeExtension.class)
                .addClass(DummyScoped.class)
                .addClass(DummyContext.class)
                .addPackage(WebProfileCategory.class.getPackage());

        JavaArchive[] coreArchives = ShrinkWrapArchiveUtil.getArchives(null,
                "META-INF/beans.xml",
                new String[]{"org.apache.deltaspike.core", "org.apache.deltaspike.test.category"},
                excludedPackagesOrFiles,
                "ds-core");

        List<JavaArchive> archives = new ArrayList<JavaArchive>(Arrays.asList(coreArchives));
        archives.add(extensionsJar);
        return archives.toArray(new JavaArchive[archives.size()]);
    }
}
