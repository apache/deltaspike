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
package org.apache.deltaspike.test.core.api.message.locale;

import java.util.Date;

import org.apache.deltaspike.core.api.message.Message;
import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

@MessageBundle
public interface MessageWithLocale
{
    @MessageTemplate("Welcome to DeltaSpike")
    String welcomeToDeltaSpike();

    @MessageTemplate("Welcome to %s")
    Message welcomeTo(MessageContext messageContext, String name);

    @MessageTemplate("Welcome to %s")
    String welcomeWithStringVariable(String name);

    @MessageTemplate("Welcome %f")
    String welcomeWithFloatVariable(Float floatValue);

    @MessageTemplate("Welcome %1$tB %1$te,%1$tY")
    String welcomeWithDateVariable(Date dateValue);
}
