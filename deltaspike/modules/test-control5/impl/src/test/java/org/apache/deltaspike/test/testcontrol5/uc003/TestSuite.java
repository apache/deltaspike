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
package org.apache.deltaspike.test.testcontrol5.uc003;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

//Usually NOT needed! Currently only needed due to our arquillian-setup
@Tag("SeCategory")

/**
 * JUnit 5 replacement for the JUnit 4 Suite.
 * Each test class with @ExtendWith(CdiTestExtension.class) shares the same CDI container.
 */
public class TestSuite
{
    @Test
    void suiteMarker()
    {
        // This class is a marker for the test suite.
        // The actual tests are in RequestAndSessionScopePerTestMethodTest and SessionScopePerTestClassTest.
        // They share the same container via CdiTestExtension.
    }
}
