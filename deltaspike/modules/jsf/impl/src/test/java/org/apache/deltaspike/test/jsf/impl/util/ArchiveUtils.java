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
package org.apache.deltaspike.test.jsf.impl.util;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.utils.ShrinkWrapArchiveUtil;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains helpers for building frequently used archives
 */
public class ArchiveUtils
{
    private ArchiveUtils() 
    {
        // private ct
    }
    
    public static JavaArchive[] getDeltaSpikeCoreAndJsfArchive()
    {
        String[] excludedFiles;

        excludedFiles = new String[]{"META-INF.apache-deltaspike.properties"};

        // we also need quite some internal Arquillian classes on the client side
        // this JAR has NO beans.xml to prevent class scanning!
        JavaArchive grapheneJar = ShrinkWrap
                .create(JavaArchive.class, "deltaspikeUtils.jar")
                .addClass(ArchiveUtils.class)
                .addPackages(true, "org.jboss.arquillian.graphene")
                .addPackages(true, "org.jboss.arquillian.ajocado")
                .addPackages(true, "org.openqa.selenium")
                .addPackage(WebProfileCategory.class.getPackage());

        JavaArchive[] coreArchives = ShrinkWrapArchiveUtil.getArchives(null
                , "META-INF/beans.xml"
                , new String[]{ "org.apache.deltaspike.core",
                                "org.apache.deltaspike.proxy",
                                "org.apache.deltaspike.jsf" }
                , excludedFiles,
                "ds-core_proxy_jsf");

        List<JavaArchive> archives = new ArrayList<JavaArchive>(Arrays.asList(coreArchives));
        archives.add(grapheneJar);
        return archives.toArray(new JavaArchive[archives.size()]);
    }

    public static JavaArchive[] getDeltaSpikeSecurityArchive()
    {
        String[] excludedFiles;

        excludedFiles = new String[]{"META-INF.apache-deltaspike.properties"};


        return ShrinkWrapArchiveUtil.getArchives(null,
                "META-INF/beans.xml",
                new String[]{"org.apache.deltaspike.security"}, excludedFiles,
                "ds-security");
    }

    public static Asset getBeansXml()
    {
        Asset beansXml = new StringAsset(
            "<beans>" +
                "<interceptors>" +
                    "<class>org.apache.deltaspike.jsf.impl.config.view.navigation.NavigationParameterInterceptor</class>" +
                    "<class>org.apache.deltaspike.jsf.impl.config.view.navigation.NavigationParameterListInterceptor</class>" +
                "</interceptors>" +
            "</beans>"
        );

        return beansXml;
    }
}
