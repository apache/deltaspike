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
package org.apache.deltaspike.test.jpa.datasource;


import org.apache.deltaspike.jpa.api.datasource.DataSourceConfig;

import javax.enterprise.context.ApplicationScoped;
import java.util.Properties;

/**
 * {@link DataSourceConfig} for our test database.
 */
@ApplicationScoped
public class LocalDatabaseConfig implements DataSourceConfig
{
    public String getJndiResourceName(String connectionId)
    {
        return null;
    }

    public String getConnectionClassName(String connectionId)
    {
        return "org.hsqldb.jdbcDriver";
    }

    public String getJdbcConnectionUrl(String connectionId)
    {
        return "jdbc:hsqldb:mem:test";
    }

    public Properties getConnectionProperties(String connectionId)
    {
        Properties props = new Properties();

        props.put("userName", "sa");
        props.put("", "");

        return props;
    }

}
