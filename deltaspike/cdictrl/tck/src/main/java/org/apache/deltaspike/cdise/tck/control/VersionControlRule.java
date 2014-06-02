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
package org.apache.deltaspike.cdise.tck.control;

import org.apache.deltaspike.test.utils.CdiContainerUnderTest;
import org.apache.deltaspike.test.utils.CdiImplementation;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


/** 
 * 
 * This {@link TestRule} allows an test to check those methods annotated with {@link LockedCDIImplementation}.
 * 
 * You can define some {@link LockedCDIImplementation}. Example: 
 * 
 * <pre>
 *     @LockedCDIImplementation(cdiImplementations = {
 *           CdiImplementation.OWB11,
 *           CdiImplementation.OWB12
 *   })
 * </pre>
 *  
 * 
 * You can also define specific versions of an specific implementation. Example:
 * 
 * <pre>
 *     @LockedCDIImplementation(versions = {
 *           @LockedVersionRange(implementation = CdiImplementation.WELD11, versionRange = "[1.1.13,1.2)"),
 *           @LockedVersionRange(implementation = CdiImplementation.WELD20, versionRange = "[2.0.1.Final,2.1)")
 *           })
 * </pre>
 * 
 * @author rafaelbenevides
 *
 */
public class VersionControlRule implements TestRule
{

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return new Statement()
        {

            @Override
            public void evaluate() throws Throwable
            {
                LockedCDIImplementation lockedCDIImplAnnotation = description
                        .getAnnotation(LockedCDIImplementation.class);
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

            private void checkAnnotation(LockedCDIImplementation lockedCDIImplAnnotation, Statement base)
                throws Throwable
            {
                CdiImplementation[] implementations = lockedCDIImplAnnotation.cdiImplementations();
                for (CdiImplementation cdiImpl : implementations)
                {
                    String versionRange = getLockedVersionRange(lockedCDIImplAnnotation, cdiImpl);
                    if (CdiContainerUnderTest.isCdiVersion(cdiImpl, versionRange))
                    {
                        base.evaluate();
                    }
                }
            }

            /**
             * Get the locked version Range
             * 
             * @param lockedCDIImplAnnotation
             * @param cdiImpl
             * @return the locked version range
             */
            private String getLockedVersionRange(LockedCDIImplementation lockedCDIImplAnnotation,
                    CdiImplementation cdiImpl)
            {
                LockedVersionRange[] versions = lockedCDIImplAnnotation.versions();
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
}
