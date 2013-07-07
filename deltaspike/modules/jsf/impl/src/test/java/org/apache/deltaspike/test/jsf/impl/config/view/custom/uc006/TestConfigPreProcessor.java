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
package org.apache.deltaspike.test.jsf.impl.config.view.custom.uc006;

import org.apache.deltaspike.core.spi.config.view.ConfigPreProcessor;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.api.literal.ViewLiteral;

public class TestConfigPreProcessor implements ConfigPreProcessor<View>
{
    @Override
    public View beforeAddToConfig(View metaData, ViewConfigNode viewConfigNode)
    {
        return new ViewLiteral("/test/", "view", "custom", View.NavigationMode.DEFAULT, View.ViewParameterMode.DEFAULT,
            View.DefaultBasePathBuilder.class, View.DefaultFileNameBuilder.class, View.DefaultExtensionBuilder.class);
    }
}
