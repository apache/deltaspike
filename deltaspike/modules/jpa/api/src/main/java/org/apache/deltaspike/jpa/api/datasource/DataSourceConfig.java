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
package org.apache.deltaspike.jpa.api.datasource;

import org.apache.deltaspike.core.api.config.DeltaSpikeConfig;

import java.util.Properties;

/**
 * <h3>Configuration for a dynamic DataSource.</h3>
 * <p>If you use the ConfigurableDataSource then this interface needs
 * to be implemented in customer projects to return
 * the proper values to connect to the database.</p>
 *
 * <p>The <code>connectionId</code> parameter can be used to distinguish
 * between different databases.</p>
 *
 * <p>There are 3 ways to configure a DataSource
 *
 * <ol>
 *     <li>
 *         via JNDI lookup - specify the JNDI resource location for the DataSource via
 *         {@link #getJndiResourceName(String)}
 *     </li>
 *     <li>
 *         via a DataSource class name plus properties - This will be used if {@link #getJndiResourceName(String)}
 *         returns <code>null</code>. In this case you must specify the {@link #getConnectionClassName(String)}
 *         to contain the class name of a DataSource, e.g.
 *         <code>&quot";com.mchange.v2.c3p0.ComboPooledDataSource&quot";</code>
 *         and return additional configuration via {@link #getConnectionProperties(String)}.
 *     </li>
 *     <li>
 *         via a JDBC Driver class name plus properties - This will be used if {@link #getJndiResourceName(String)}
 *         returns <code>null</code>. In this case you must specify the {@link #getConnectionClassName(String)}
 *         to contain the class name of a javax.sql.Driver, e.g.
 *         <code>&quot";org.hsqldb.jdbcDriver&quot";</code>
 *         and return additional configuration via {@link #getConnectionProperties(String)}.
 *     </li>
 * </ol>
 * </p>
 *
 * <h3>Usage</h3>
 * <p>Instead of configuring any hardcoded DataSource provider, JDBC driver
 * or JNDI location of the DataSource you just configure our <i>ConfigurableDataSource</i>
 * in your persistence.xml. This class is an implementation of DataSource and acts as
 * kind of a proxy to determine the underlying database configuration for your usage
 * scenarios.</p>
 * <p>A possible persistence.xml configuration would look like the following:
 * <pre>
 * &lt;persistence xmlns=&quot;http://java.sun.com/xml/ns/persistence&quot;
 *              xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot;
 *              xsi:schemaLocation=&quot;http://java.sun.com/xml/ns/persistence
 *                         http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd&quot;
 *              version=&quot;1.0&quot;&gt;
 *
 *     &lt;persistence-unit name=&quot;test&quot; &gt;
 *         &lt;provider&gt;org.apache.openjpa.persistence.PersistenceProviderImpl&lt;/provider&gt;
 *
 *         &lt;class&gt;org.apache.deltaspike.jpa.test.TestEntity&lt;/class&gt;
 *
 *         &lt;properties&gt;
 *             &lt;property name=&quot;openjpa.ConnectionDriverName&quot;
 *                  value=&quot;org.apache.deltaspike.jpa.impl.datasource.ConfigurableDataSource&quot;/&gt;
 *             &lt;property name=&quot;openjpa.ConnectionProperties&quot;
 *                  value=&quot;connectionId=core&quot;/&gt;
 *         &lt;/properties&gt;
 *
 *     &lt;/persistence-unit&gt;
 * &lt;/persistence&gt;
 * </pre>
 *
 * </p>
 *
 */
public interface DataSourceConfig extends DeltaSpikeConfig
{

    /**
     * Return the JNDI resource name if the DataSource should get retrieved via JNDI.
     * If a native JDBC connection should get used, this method must return <code>null</code>.
     * And the JDBC connection properties must get set via
     * {@link #getConnectionClassName(String)} and {@link #getConnectionProperties(String)}.
     *
     * @param connectionId used to distinguish between different databases.
     *
     * @return the JNDI lookup for the DataSource or <code>null</code> if a native
     *      JDBC connection should get used.
     */
    String getJndiResourceName(String connectionId);

    /**
     * @param connectionId used to distinguish between different databases.
     *
     * @return the fully qualified class name of the JDBC driver for the underlying connection
     *      or <code>null</code> if {@link #getJndiResourceName(String)} is not being used
     */
    String getConnectionClassName(String connectionId);

    /**
     * @param connectionId used to distinguish between different databases.
     *
     * @return allows to configure additional connection properties which will
     *      get applied to the underlying JDBC driver or <code>null</code>
     *      if {@link #getJndiResourceName(String)} is not being used
     */
    Properties getConnectionProperties(String connectionId);

    /**
     * This will only get used if {@link #getConnectionClassName(String)} is a javax.sql.Driver.
     * Foor Datasources, the underlying connection url must get configured via
     * {@link #getConnectionProperties(String)}.
     *
     * @param connectionId used to distinguish between different databases.
     *
     * @return the connection url, e.g. &quot;jdbc://...&quot;
     *      or <code>null</code> if {@link #getJndiResourceName(String)} is not being used
     */
    String getJdbcConnectionUrl(String connectionId);
}
