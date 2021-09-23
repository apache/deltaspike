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

package org.apache.deltaspike.test.core.impl.exception.control.traversal;

public class Exceptions
{
    public static class Exception1 extends Exception
    {
        private static final long serialVersionUID = 3748419159086636984L;

        public Exception1(Throwable cause)
        {
            super(cause);
        }
    }

    public static class Exception2 extends Exception
    {
        private static final long serialVersionUID = 7151417049655860515L;

        public Exception2(Throwable cause)
        {
            super(cause);
        }
    }

    public static class Exception3Super extends Exception
    {
        private static final long serialVersionUID = 1886009541068471679L;
    }

    public static class Exception3 extends Exception3Super
    {
        private static final long serialVersionUID = -7924704072611802705L;
    }
}
