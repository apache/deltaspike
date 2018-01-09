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
package org.apache.deltaspike.test.proxy.impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import org.apache.deltaspike.test.utils.ShrinkWrapArchiveUtil;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public abstract class ArchiveUtils
{
    private ArchiveUtils()
    {
    }

    public static JavaArchive[] getDeltaSpikeCoreAndProxyArchive()
    {
        ArrayList<JavaArchive> result = new ArrayList<JavaArchive>();

        JavaArchive[] temp;

        temp = ShrinkWrapArchiveUtil.getArchives(null,
                "META-INF/beans.xml",
                new String[] { "org.apache.deltaspike.core",
                        "org.apache.deltaspike.proxy",
                        "org.apache.deltaspike.test.category"},
                new String[] { "META-INF.apache-deltaspike.properties" },
                "ds-core_and_proxy");
        result.addAll(Arrays.asList(temp));

        // add asm - it isn't shaded yet in the test phase
        result.add(ShrinkWrap.create(JavaArchive.class, "ds-asm.jar")
                    .addPackages(true, "org.objectweb.asm"));

        return result.toArray(new JavaArchive[result.size()]);
    }
}
