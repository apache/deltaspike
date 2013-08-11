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
package org.apache.deltaspike.test.utils;

/**
 * A small helper class which checks if the container
 * which is currently being tested matches the given version RegExp
 */
public class CdiContainerUnderTest
{
    private CdiContainerUnderTest()
    {
        // utility class ct
    }

    /**
     * Checks whether the current container matches the given version regexps.
     * @param containerRegExps container versions to test against.
     *                         e.g. 'owb-1\\.0\\..*' or 'weld-2\\.0\\.0\\..*'
     */
    public static boolean is(String... containerRegExps)
    {
        String containerVersion = System.getProperty("cdicontainer.version");

        if (containerVersion == null)
        {
            return false;
        }

        for (String containerRe : containerRegExps)
        {
            if (containerVersion.matches(containerRe))
            {
                return true;
            }
        }

        return false;
    }
}
