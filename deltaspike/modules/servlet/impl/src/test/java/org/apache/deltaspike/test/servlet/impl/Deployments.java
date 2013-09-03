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
package org.apache.deltaspike.test.servlet.impl;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class Deployments
{

    public static Collection<JavaArchive> getDeltaSpikeCoreArchives()
    {

        JavaArchive coreApiArchive = ShrinkWrap.create(JavaArchive.class)
                .as(ExplodedImporter.class)
                .importDirectory("../../../core/api/target/classes")
                .as(JavaArchive.class);

        JavaArchive coreImplArchive = ShrinkWrap.create(JavaArchive.class)
                .as(ExplodedImporter.class)
                .importDirectory("../../../core/impl/target/classes")
                .as(JavaArchive.class);

        return Arrays.asList(coreApiArchive, coreImplArchive);
    
    }

    public static Collection<JavaArchive> getDeltaSpikeServletArchives()
    {

        JavaArchive servletApiArchive = ShrinkWrap.create(JavaArchive.class)
                .as(ExplodedImporter.class)
                .importDirectory("../api/target/classes")
                .as(JavaArchive.class);

        JavaArchive servletImplArchive = ShrinkWrap.create(JavaArchive.class)
                .as(ExplodedImporter.class)
                .importDirectory("../impl/target/classes")
                .as(JavaArchive.class);

        return Arrays.asList(servletApiArchive, servletImplArchive);

    }

    /**
     * @return
     */
    public static JavaArchive getTestSupportArchives()
    {
        return ShrinkWrap.create(JavaArchive.class, "utils.jar")
                .addPackages(true, "org.apache.http")  
                .addPackages(true, "org.apache.deltaspike.test.category")
                ;
    }

}
