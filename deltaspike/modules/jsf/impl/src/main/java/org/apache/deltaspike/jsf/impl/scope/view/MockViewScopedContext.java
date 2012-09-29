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
package org.apache.deltaspike.jsf.impl.scope.view;

import javax.enterprise.inject.spi.BeanManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * <p>This is a Mock version of the {@link ViewScopedContext}.
 * It will automatically get used instead of it's parent class
 * if we are in the
 * {@link javax.faces.application.ProjectStage#UnitTest}.</p>
 *
 * <p>There is of course no automatic cleaning if we transit over to the
 * next view! In this case you can use {@link #resetViewMap()} to clean
 * the mock ViewMap manually in your unit test.</p>
 *
 * <p><b>Attention:</b> The ViewMap is a shared static ConcurrentHashMap,
 * so this implementation is not able to emulate the behaviour of multiple views
 * at the same time!</p>
 */
public class MockViewScopedContext extends ViewScopedContext
{

    private static Map<String, Object> mockViewMap = new ConcurrentHashMap<String, Object>();

    public MockViewScopedContext(BeanManager beanManager)
    {
        super(beanManager);
    }

    /**
     * Simply clear the mock ViewMap.
     * This function should get called if the same unit test needs to test multiple
     * views and also between different tests if the container doesn't get restarted
     * in between.
     */
    public static void resetViewMap()
    {
        mockViewMap.clear();
    }

    @Override
    protected Map<String, Object> getViewMap()
    {
        return mockViewMap;
    }

    /**
     * @return always <code>true</code> since in a unit test it's always active.
     */
    @Override
    public boolean isActive()
    {
        return true;
    }
}
