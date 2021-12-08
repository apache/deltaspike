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
package org.apache.deltaspike.test.core.api.config.beans;

import org.apache.deltaspike.core.api.config.ConfigProperty;

/**
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class ServerEndpointPojoWithCt extends ServerEndpointPojoWithFields
{
    public ServerEndpointPojoWithCt(@ConfigProperty(name = "host") String host,
                                    @ConfigProperty(name = "port") Integer port,
                                    @ConfigProperty(name = "path") String path)
    {
        this.host = host;
        this.port = port;
        this.path = path;
    }

}
