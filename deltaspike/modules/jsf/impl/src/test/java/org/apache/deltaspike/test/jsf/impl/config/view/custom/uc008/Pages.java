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
package org.apache.deltaspike.test.jsf.impl.config.view.custom.uc008;

import org.apache.deltaspike.core.api.config.view.ViewConfig;

interface Pages extends ViewConfig
{
    class Index implements Pages
    {
    }

    @TestMenuEntry(pos = 1)
    interface Section1 extends Pages
    {
        @TestMenuEntry(pos = 1)
        class Content1 implements Section1
        {
        }

        @TestMenuEntry(pos =2)
        class Content2 implements Section1
        {
        }
    }

    @TestMenuEntry(pos = 2)
    interface Section2 extends Pages
    {
        @TestMenuEntry(pos = 1)
        class Content1 implements Section2
        {
        }

        @TestMenuEntry(pos =2)
        class Content2 implements Section2
        {
        }
    }
}
