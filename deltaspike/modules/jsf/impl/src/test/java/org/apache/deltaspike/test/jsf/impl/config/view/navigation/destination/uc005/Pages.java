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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.destination.uc005;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;

interface Pages
{
    interface Wizard1
    {
        @View(basePath = "")
        class Step1 implements ViewConfig
        {
        }

        @View(basePath = "/")
        class Step2 implements ViewConfig
        {
        }

        @View(basePath = "./")
        class Step3 implements ViewConfig
        {
        }

        @View(basePath = "w1/")
        class Step4 implements ViewConfig
        {
        }

        @View(basePath = "/w1/")
        class Step5 implements ViewConfig
        {
        }

        @View(basePath = "./w1b/")
        class Step6 implements ViewConfig
        {
        }

        @View(basePath = "w1b")
        class Step7 implements ViewConfig
        {
        }
    }

    @View(basePath = "w2") //gets ignored because @Folder should be used instead
    interface Wizard2
    {
        @View(basePath = "")
        class Step1 implements ViewConfig
        {
        }

        @View(basePath = "/")
        class Step2 implements ViewConfig
        {
        }

        @View(basePath = "./")
        class Step3 implements ViewConfig
        {
        }

        @View(basePath = "w2/")
        class Step4 implements ViewConfig
        {
        }

        @View(basePath = "/w2/")
        class Step5 implements ViewConfig
        {
        }

        @View(basePath = "./w2b/")
        class Step6 implements ViewConfig
        {
        }

        @View(basePath = "w2b")
        class Step7 implements ViewConfig
        {
        }
    }

    @View(basePath = "/w3") //gets ignored because @Folder should be used instead
    interface Wizard3
    {
        @View(basePath = "")
        class Step1 implements ViewConfig
        {
        }

        @View(basePath = "/")
        class Step2 implements ViewConfig
        {
        }
    }

    @View(basePath = "./w4b") //gets ignored because @Folder should be used instead
    interface Wizard4
    {
        @View(basePath = "")
        class Step1 implements ViewConfig
        {
        }

        @View(basePath = "/")
        class Step2 implements ViewConfig
        {
        }
    }

    @View(basePath = "w5/") //gets ignored because @Folder should be used instead
    interface Wizard5
    {
        class Step1 implements ViewConfig
        {
        }
    }

    @View(basePath = "/w6/") //gets ignored because @Folder should be used instead
    interface Wizard6
    {
        class Step1 implements ViewConfig
        {
        }
    }

    @View(basePath = "./w7b/") //gets ignored because @Folder should be used instead
    interface Wizard7
    {
        class Step1 implements ViewConfig
        {
        }
    }

    @Folder(name = "w8/")
    interface Wizard8
    {
        class Step1 implements ViewConfig
        {
        }
    }

    @Folder(name = "/w9/")
    interface Wizard9
    {
        class Step1 implements ViewConfig
        {
        }
    }

    @Folder(name = "./w10a/")
    interface Wizard10
    {
        class Step1 implements ViewConfig
        {
        }
    }

    @Folder(name = "w11/")
    interface Wizard11
    {
        @View(basePath = "")
        class Step1 implements ViewConfig
        {
        }

        @View(basePath = "/")
        class Step2 implements ViewConfig
        {
        }

        @View(basePath = "./")
        class Step3 implements ViewConfig
        {
        }

        @View(basePath = "w11b/")
        class Step4 implements ViewConfig
        {
        }

        @View(basePath = "/w11b/")
        class Step5 implements ViewConfig
        {
        }

        @View(basePath = "./w11b/")
        class Step6 implements ViewConfig
        {
        }

        @View(basePath = "w11b")
        class Step7 implements ViewConfig
        {
        }
    }

    @Folder(name = "/w12/")
    interface Wizard12
    {
        @View(basePath = "")
        class Step1 implements ViewConfig
        {
        }

        @View(basePath = "/")
        class Step2 implements ViewConfig
        {
        }

        @View(basePath = "./")
        class Step3 implements ViewConfig
        {
        }

        @View(basePath = "w12b/")
        class Step4 implements ViewConfig
        {
        }

        @View(basePath = "/w12b/")
        class Step5 implements ViewConfig
        {
        }

        @View(basePath = "./w12b/")
        class Step6 implements ViewConfig
        {
        }

        @View(basePath = "w12b")
        class Step7 implements ViewConfig
        {
        }
    }

    @Folder(name = "./w13a/")
    interface Wizard13
    {
        @View(basePath = "")
        class Step1 implements ViewConfig
        {
        }

        @View(basePath = "/")
        class Step2 implements ViewConfig
        {
        }

        @View(basePath = "./")
        class Step3 implements ViewConfig
        {
        }

        @View(basePath = "w13b/")
        class Step4 implements ViewConfig
        {
        }

        @View(basePath = "/w13b/")
        class Step5 implements ViewConfig
        {
        }

        @View(basePath = "./w13b/")
        class Step6 implements ViewConfig
        {
        }

        @View(basePath = "w13b")
        class Step7 implements ViewConfig
        {
        }
    }

    @Folder(name = "./")
    interface Wizard14
    {
        @View(basePath = "")
        class Step1 implements ViewConfig
        {
        }
    }
}
