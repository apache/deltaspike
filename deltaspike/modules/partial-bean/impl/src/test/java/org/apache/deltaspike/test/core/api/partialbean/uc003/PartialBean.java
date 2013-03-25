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
package org.apache.deltaspike.test.core.api.partialbean.uc003;

import org.apache.deltaspike.test.core.api.partialbean.shared.CustomInterceptor;
import org.apache.deltaspike.test.core.api.partialbean.shared.TestPartialBeanBinding;
import org.apache.deltaspike.test.core.api.partialbean.shared.TestInterceptorAware;
import org.apache.deltaspike.test.core.api.partialbean.shared.TestBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

@TestPartialBeanBinding
@RequestScoped
@CustomInterceptor //doesn't work currently
public abstract class PartialBean implements /*just needed for testing interceptors: */ TestInterceptorAware
{
    @Inject
    private TestBean testBean;

    private String value;
    private boolean intercepted;

    public abstract String getResult();

    @PostConstruct
    protected void onCreate()
    {
        this.value = "manual";
    }

    @PreDestroy
    protected void onDestroy()
    {
        //TODO check in a test
    }

    public String getManualResult()
    {
        return this.value + "-" + this.testBean.getValue() + "-" + this.intercepted;
    }

    @Override
    public void setIntercepted(boolean intercepted)
    {
        this.intercepted = intercepted;
    }
}