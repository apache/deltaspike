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
package org.apache.deltaspike.jpa.impl.datasource;


import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.impl.util.JndiUtils;
import org.apache.deltaspike.jpa.api.datasource.DataSourceConfig;

/**
 * <p>This class can be used instead of a real DataSource.
 * It is a simple wrapper to hide any database configuration details
 * and make it configurable via CDI.</p>
 * <p>See {@link DataSourceConfig} on how to configure it!</p>
 *
 * <p>The configuration itself will be provided via CDI mechanics.
 * To distinguish different databases, users can specify a
 * <code>connectionId</code>. If no <code>connectionId</code> is set,
 * the String <code>default</code> will be used</p>
 */
public class ConfigurableDataSource implements DataSource
{
    /**
     * config and settings are loaded only once.
     */
    private volatile boolean loaded;

    /**
     * The connectionId allows to configure multiple databases.
     * This can e.g. be used to distinguish between a 'customer' and 'admin'
     * database.
     */
    private String connectionId = "default";

    /**
     * The underlying configuration of the datasource
     */
    private DataSourceConfig dataSourceConfig;

    /**
     * In case of an underlying JDBC connection, we need to provide the connection URL;
     */
    private String jdbcConnectionURL;

    /**
     * In case of an underlying JDBC connection, we need to provide some configured properties
     */
    private Properties connectionProperties;

    /**
     *  The underlying 'real' DataSource if we got a DataSource either via JNDI
     *  or as class name.
     */
    private DataSource wrappedDataSource = null;

    /**
     *  The underlying jdbcDriver if configured.
     */
    private Driver wrappedJdbcDriver = null;


    public ConfigurableDataSource()
    {
        loaded = false;
        dataSourceConfig = BeanProvider.getContextualReference(DataSourceConfig.class);
    }

    public void setConnectionId(String connectionId)
    {
        if (loaded)
        {
            throw new IllegalStateException("connectionId must not get changed after the DataSource was established");
        }
        this.connectionId = connectionId;
    }

    public Connection getConnection() throws SQLException
    {
        return getConnection(null, null);
    }

    public Connection getConnection(String userName, String password) throws SQLException
    {
        if (!loaded)
        {
            initDataSource();
        }

        if (wrappedDataSource != null)
        {
            // if we got a DataSource as underlying connector
            if (userName == null && password == null )
            {
                return wrappedDataSource.getConnection();
            }
            return wrappedDataSource.getConnection(userName, password);
        }
        else if (wrappedJdbcDriver != null)
        {
            // if we got a native JDBC Driver class as underlying connector
            return wrappedJdbcDriver.connect(jdbcConnectionURL, connectionProperties);
        }

        return null;
    }


    public PrintWriter getLogWriter() throws SQLException
    {
        return null;
    }

    public void setLogWriter(PrintWriter printWriter) throws SQLException
    {
    }

    public void setLoginTimeout(int loginTimeout) throws SQLException
    {
    }

    public int getLoginTimeout() throws SQLException
    {
        return 0;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        if (isWrapperFor(iface))
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return iface.isAssignableFrom(ConfigurableDataSource.class);
    }

    /**
     * NEW JDK1.7 signature.
     * This makes sure that CODI can also get compiled using java-7.
     * This method is not actively used though.
     */
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     *  Initialize the DataSource either from JNDI or via JDBC Driver.
     *  This method does not actually create a connection yet.
     */
    protected synchronized void initDataSource() throws SQLException
    {
        // double check lock idiom on volatile member is ok as of Java5
        if (loaded)
        {
            return;
        }
        loaded = true;

        String jndiLookupName = dataSourceConfig.getJndiResourceName(connectionId);
        if (jndiLookupName != null && jndiLookupName.length() > 0)
        {
            wrappedDataSource = JndiUtils.lookup(jndiLookupName, DataSource.class);
            return;
        }

        // no JNDI, so we take the direct JDBC route.
        String dataSourceClass = dataSourceConfig.getConnectionClassName(connectionId);

        if (dataSourceClass == null || dataSourceClass.length() == 0)
        {
            throw new SQLException("Neither a JNDI location nor a JDBC driver class name is configured!");
        }

        connectionProperties = dataSourceConfig.getConnectionProperties(connectionId);

        try
        {
            // we explicitely use class.forName and NOT the ThreadContextClassLoader!
            Class clazz =  Class.forName(dataSourceClass);

            // the given driver classname must be a DataSource
            if (DataSource.class.isAssignableFrom(clazz))
            {
                wrappedDataSource = (DataSource) clazz.newInstance();

                for (Map.Entry configOption : connectionProperties.entrySet())
                {
                    String name = (String) configOption.getKey();
                    String value = (String) configOption.getValue();
                    setProperty(wrappedDataSource, name, value);
                }
            }
            else if (Driver.class.isAssignableFrom(clazz))
            {
                // if we have a javax.sql.Driver then we also need an explicite connection URL
                jdbcConnectionURL = dataSourceConfig.getJdbcConnectionUrl(connectionId);
                if (jdbcConnectionURL == null)
                {
                    throw new SQLException("Neither a JNDI location nor a JDBC connection URL is configured!");
                }

                wrappedJdbcDriver = (Driver) clazz.newInstance();
            }
            else
            {
                throw new SQLException("Configured DriverClassName is not a javax.sql.DataSource "
                        + "nor a javax.sql.Driver: "
                        + dataSourceClass);
            }
        }
        catch (RuntimeException e)
        {
            wrappedDataSource = null;
            throw e;
        }
        catch (SQLException e)
        {
            wrappedDataSource = null;
            throw e;
        }
        catch (Exception e)
        {
            wrappedDataSource = null;
            throw new RuntimeException(e);
        }
    }

    protected void setProperty(Object instance, String key, String value)
        throws InvocationTargetException, IllegalAccessException
    {
        if (key.length() == 0)
        {
            throw new IllegalArgumentException("property name must not be empty!");
        }

        String setterName = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
        Method setter = null;
        try
        {
            setter = instance.getClass().getMethod(setterName, String.class);
        }
        catch (NoSuchMethodException e)
        {
            try
            {
                setter = instance.getClass().getMethod(setterName, Object.class);
            }
            catch (NoSuchMethodException e1)
            {
                //X TODO probably search for fields to set

            }
        }

        if (setter == null)
        {
            return;
        }

        if (!setter.isAccessible())
        {
            setter.setAccessible(true);
        }

        setter.invoke(instance, value);
    }

}
