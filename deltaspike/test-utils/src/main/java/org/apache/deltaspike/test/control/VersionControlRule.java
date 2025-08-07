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
package org.apache.deltaspike.test.control;

import java.util.Arrays;

import org.apache.deltaspike.test.utils.CdiContainerUnderTest;
import org.apache.deltaspike.test.utils.Implementation;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


/** 
 * 
 * This {@link TestRule} allows a test to check those methods annotated with {@link LockedImplementation}.
 * 
 * You can define some {@link LockedImplementation}. Example:
 * 
 * <pre>
 *     @LockedCDIImplementation(cdiImplementations = {
 *           CdiImplementation.OWB11,
 *           CdiImplementation.OWB12
 *   })
 * </pre>
 *  
 * 
 * You can also define specific versions of a specific implementation. Example:
 * 
 * <pre>
 *     @LockedCDIImplementation(versions = {
 *           @LockedVersionRange(implementation = CdiImplementation.WELD11, versionRange = "[1.1.13,1.2)"),
 *           @LockedVersionRange(implementation = CdiImplementation.WELD20, versionRange = "[2.0.1.Final,2.1)")
 *           })
 * </pre>
 * 
 * @author rafaelbenevides
 * @author struberg
 */
public class VersionControlRule implements TestRule
{
    private static final Implementation[] EMPTY_IMPL = new Implementation[0];

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return new Statement()
        {

            @Override
            public void evaluate() throws Throwable
            {
                LockedImplementation lockedCDIImplAnnotation = description
                        .getAnnotation(LockedImplementation.class);
                // no @LockedCDIImplementation present or if running specified Container
                if (lockedCDIImplAnnotation == null)
                {
                    base.evaluate();
                }
                else
                {
                    checkAnnotation(lockedCDIImplAnnotation, base);
                }
            }

            private void checkAnnotation(LockedImplementation lockedImplAnnotation, Statement base)
                throws Throwable
            {
                Implementation[] implementations = getImplementations(lockedImplAnnotation.implementations(), lockedImplAnnotation.versions());

                // Run the test if there is no explicit list of implementations to only run on.
                boolean shouldRun = implementations.length == 0;

                for (Implementation impl : implementations)
                {
                    String versionRange = getLockedVersionRange(lockedImplAnnotation.versions(), impl);
                    if (CdiContainerUnderTest.isImplementationVersion(impl, versionRange))
                    {
                        shouldRun = true;
                    }
                }

                // now check the exclude list: implementations to NOT run on!
                final Implementation[] excludedImplementations = getImplementations(lockedImplAnnotation.excludedImplementations(),
                                                                                    lockedImplAnnotation.excludedVersions());
                for (Implementation impl : excludedImplementations)
                {
                    String versionRange = getLockedVersionRange(lockedImplAnnotation.excludedVersions(), impl);
                    if (CdiContainerUnderTest.isImplementationVersion(impl, versionRange))
                    {
                        shouldRun = false;
                    }
                }

                if (shouldRun)
                {
                    base.evaluate();
                }
            }

            /**
             * Get the locked version Range
             * 
             * @return the locked version range
             */
            private String getLockedVersionRange(LockedVersionRange[] versions,
                                                 Implementation cdiImpl)
            {
                for (LockedVersionRange versionRange : versions)
                {
                    if (versionRange.implementation().equals(cdiImpl))
                    {
                        return versionRange.versionRange();
                    }
                }
                return null;
            }
        };
    }

    private Implementation[] getImplementations(Implementation[] implementations, LockedVersionRange[] lockedVersionRanges)
    {
        if (implementations != null && implementations.length > 0)
        {
            return implementations;
        }

        if (lockedVersionRanges != null && lockedVersionRanges.length > 0)
        {
            return Arrays.stream(lockedVersionRanges)
                .map(lr -> lr.implementation())
                .distinct()
                .toArray(Implementation[]::new);
        }

        return EMPTY_IMPL;
    }
}
