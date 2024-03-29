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
package org.apache.deltaspike.cdise.tck.beans;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;

@SessionScoped
public class TestUser implements Serializable
{
    private static final long serialVersionUID = -4171521313675763849L;
    private static ThreadLocal<Boolean> preDestroyCalled = new ThreadLocal<Boolean>();

    private String name;

    @PostConstruct
    protected void onPostConstruct()
    {
        //reset it
        preDestroyCalled.remove();
        preDestroyCalled.set(false);
    }

    @PreDestroy
    protected void onPreDestroy()
    {
        preDestroyCalled.set(true);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public static boolean isPreDestroyCalled()
    {
        return preDestroyCalled.get();
    }
}
