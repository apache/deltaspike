package org.apache.deltaspike.test.core.api.config.propertyconfigsource;

import org.apache.deltaspike.core.api.config.PropertyConfigSource;

/**
 * Custom
 */
public class MyCustomPropertyConfigSource implements PropertyConfigSource
{
    @Override
    public String getPropertyFileName()
    {
        return "myconfig.properties";
    }
}
