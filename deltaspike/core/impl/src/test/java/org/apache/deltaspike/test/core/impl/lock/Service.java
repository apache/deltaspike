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
package org.apache.deltaspike.test.core.impl.lock;

import org.apache.deltaspike.core.api.lock.Locked;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.deltaspike.core.api.lock.Locked.Operation.WRITE;
import static org.junit.Assert.fail;

@ApplicationScoped
public class Service {
    private final Map<String, String> entries = new HashMap<String, String>();

    @Locked(timeout = 1, timeoutUnit = TimeUnit.SECONDS)
    public String read(final String k) {
        return entries.get(k);
    }

    @Locked(timeout = 1, timeoutUnit = TimeUnit.SECONDS, operation = WRITE)
    public void write(final String k, final String v) {
        entries.put(k, v);
    }

    @Locked(operation = WRITE)
    public void force() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        } catch (final InterruptedException e) {
            Thread.interrupted();
            fail();
        }
    }
}
