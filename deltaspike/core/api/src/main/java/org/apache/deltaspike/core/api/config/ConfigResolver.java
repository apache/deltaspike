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
package org.apache.deltaspike.core.api.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Typed;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.spi.config.ConfigFilter;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.core.util.ServiceUtils;

/**
 * The main entry point to the DeltaSpike configuration mechanism.
 *
 * <p>
 * Resolves configured values of properties by going through the list of configured {@link ConfigSource}s and using the
 * one with the highest ordinal. If multiple {@link ConfigSource}s have the same ordinal, their order is undefined.</p>
 *
 * <p>
 * You can provide your own lookup paths by implementing and registering additional {@link PropertyFileConfig} or
 * {@link ConfigSource} or {@link ConfigSourceProvider} implementations.</p>
 *
 * <p>
 * The resolved configuration is also accessible by simple injection using the {@link ConfigProperty} qualifier.</p>
 *
 * @see <a href="http://deltaspike.apache.org/documentation/configuration.html">DeltaSpike Configuration Mechanism</a>
 */
@Typed()
public final class ConfigResolver
{

    private static final Logger LOG = Logger.getLogger(ConfigResolver.class.getName());

    /**
     * The content of this map will get lazily initiated and will hold the
     * sorted List of ConfigSources for each WebApp/EAR, etc (thus the
     * ClassLoader).
     */
    private static Map<ClassLoader, ConfigSource[]> configSources
        = new ConcurrentHashMap<ClassLoader, ConfigSource[]>();

    /**
     * The content of this map will hold the List of ConfigFilters
     * for each WebApp/EAR, etc (thus the ClassLoader).
     */
    private static Map<ClassLoader, List<ConfigFilter>> configFilters
        = new ConcurrentHashMap<ClassLoader, List<ConfigFilter>>();

    private static volatile ProjectStage projectStage = null;

    private ConfigResolver()
    {
        // this is a utility class which doesn't get instantiated.
    }

    /**
     * This method can be used for programmatically adding {@link ConfigSource}s.
     * It is not needed for normal 'usage' by end users, but only for Extension Developers!
     *
     * @param configSourcesToAdd the ConfigSources to add
     */
    public static synchronized void addConfigSources(List<ConfigSource> configSourcesToAdd)
    {
        // we first pickup all pre-configured ConfigSources...
        getConfigSources();

        // and now we can easily add our own
        ClassLoader currentClassLoader = ClassUtils.getClassLoader(null);
        ConfigSource[] configuredConfigSources = configSources.get(currentClassLoader);

        List<ConfigSource> allConfigSources = new ArrayList<ConfigSource>();
        allConfigSources.addAll(Arrays.asList(configuredConfigSources));
        allConfigSources.addAll(configSourcesToAdd);

        // finally put all the configSources back into the map
        configSources.put(currentClassLoader, sortDescending(allConfigSources));
    }

    /**
     * Clear all ConfigSources for the current ClassLoader.
     * This will also clean up all ConfigFilters.
     */
    public static synchronized void freeConfigSources()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);
        configSources.remove(classLoader);
        configFilters.remove(classLoader);
    }

    /**
     * Add a {@link ConfigFilter} to the ConfigResolver. This will only affect the current WebApp (or more precisely the
     * current ClassLoader and it's children).
     *
     * @param configFilter
     */
    public static void addConfigFilter(ConfigFilter configFilter)
    {
        List<ConfigFilter> currentConfigFilters = getInternalConfigFilters();
        currentConfigFilters.add(configFilter);
    }

    /**
     * @return the {@link ConfigFilter}s for the current application.
     */
    public static List<ConfigFilter> getConfigFilters()
    {
        return Collections.unmodifiableList(getInternalConfigFilters());
    }

    /**
     * @return the {@link ConfigFilter}s for the current application.
     */
    private static List<ConfigFilter> getInternalConfigFilters()
    {
        ClassLoader cl = ClassUtils.getClassLoader(null);
        List<ConfigFilter> currentConfigFilters = configFilters.get(cl);
        if (currentConfigFilters == null)
        {
            currentConfigFilters = new CopyOnWriteArrayList<ConfigFilter>();
            configFilters.put(cl, currentConfigFilters);
        }

        return currentConfigFilters;
    }

    /**
     * {@link #getPropertyValue(java.lang.String)} which returns the provided default value if no configured value can
     * be found (<code>null</code> or empty).
     *
     * @param key          the property key
     * @param defaultValue fallback value
     *
     * @return the configured property value from the {@link ConfigSource} with the highest ordinal or the defaultValue
     *         if there is no value explicitly configured
     */
    public static String getPropertyValue(String key, String defaultValue)
    {
        String value = getPropertyValue(key);

        return fallbackToDefaultIfEmpty(key, value, defaultValue);
    }

    /**
     * Resolves the value configured for the given key.
     *
     * @param key the property key
     *
     * @return the configured property value from the {@link ConfigSource} with the highest ordinal or null if there is
     *         no configured value for it
     */
    public static String getPropertyValue(String key)
    {
        ConfigSource[] appConfigSources = getConfigSources();

        String value;
        for (ConfigSource configSource : appConfigSources)
        {
            value = configSource.getPropertyValue(key);

            if (value != null)
            {
                LOG.log(Level.FINE, "found value {0} for key {1} in ConfigSource {2}.",
                        new Object[]{filterConfigValueForLog(key, value), key, configSource.getConfigName()});
                return filterConfigValue(key, value);
            }

            LOG.log(Level.FINER, "NO value found for key {0} in ConfigSource {1}.",
                    new Object[]{key, configSource.getConfigName()});
        }

        return null;
    }

    /**
     * Resolves the value configured for the given key in the current
     * {@link org.apache.deltaspike.core.api.projectstage.ProjectStage}.
     *
     * <p>
     * First, it will search for a value configured for the given key suffixed with the current ProjectStage (e.g.
     * 'myproject.myconfig.Production'), and in case this value is not found (null or empty), it will look up the given
     * key without any suffix.</p>
     *
     * <p>
     * <b>Attention</b> This method must only be used after all ConfigSources got registered and it also must not be
     * used to determine the ProjectStage itself.</p>
     *
     * @param key
     *
     * @return the value configured for {@code <given key>.<current project stage>}, or just the configured value of
     *         {@code <given key>} if the project-stage-specific value is not found (null or empty)
     *
     */
    public static String getProjectStageAwarePropertyValue(String key)
    {
        ProjectStage ps = getProjectStage();

        String value = getPropertyValue(key + '.' + ps);
        if (value == null)
        {
            value = getPropertyValue(key);
        }

        return value;
    }
    /**
     * {@link #getProjectStageAwarePropertyValue(String)} which returns the provided default value if no configured
     * value can be found (<code>null</code> or empty).
     *
     * @param key
     * @param defaultValue fallback value
     *
     * @return the configured value or if non found the defaultValue
     *
     */
    public static String getProjectStageAwarePropertyValue(String key, String defaultValue)
    {
        String value = getProjectStageAwarePropertyValue(key);

        return fallbackToDefaultIfEmpty(key, value, defaultValue);
    }

    /**
     * Resolves the value configured for the given key, parameterized by the current
     * {@link org.apache.deltaspike.core.api.projectstage.ProjectStage} and by the value of a second property.
     *
     * <p>
     * <b>Example:</b><br/>
     * Suppose the current ProjectStage is {@code UnitTest} and we are looking for the value of {@code datasource}
     * parameterized by the configured {@code dbvendor}.
     * </p>
     * <p>
     * The first step is to resolve the value of the second property, {@code dbvendor}. This will also take the current
     * ProjectStage into account. The following lookup is performed:
     * <ul><li>dbvendor.UnitTest</li></ul>
     * and if this value is not found then we will do a 2nd lookup for
     * <ul><li>dbvendor</li></ul></p>
     *
     * <p>
     * If a value was found for the second property (e.g. dbvendor = 'mysql') then we will use its value for the main
     * lookup. If no value is found for the parameterized key {@code <key>.<second property value>.<project stage>}, we
     * will do the {@code <key>.<second property value>}, then {@code <key>.<project stage>} and finally a {@code <key>}
     * lookup:
     * <ul>
     * <li>datasource.mysql.UnitTest</li>
     * <li>datasource.mysql</li>
     * <li>datasource.UnitTest</li>
     * <li>datasource</li>
     * </ul>
     * </p>
     *
     * <p>
     * <b>Attention</b> This method must only be used after all ConfigSources got registered and it also must not be
     * used to determine the ProjectStage itself.</p>
     *
     * @param key
     * @param property the property to look up first and use as the parameter for the main lookup
     *
     * @return the configured value or null if no value is found for any of the key variants
     *
     */
    public static String getPropertyAwarePropertyValue(String key, String property)
    {
        String propertyValue = getProjectStageAwarePropertyValue(property);

        String value = null;

        if (propertyValue != null && propertyValue.length() > 0)
        {
            value = getProjectStageAwarePropertyValue(key + '.' + propertyValue);
        }

        if (value == null)
        {
            value = getProjectStageAwarePropertyValue(key);
        }

        return value;
    }

    /**
     * {@link #getPropertyAwarePropertyValue(java.lang.String, java.lang.String)} which returns the provided default
     * value if no configured value can be found (<code>null</code> or empty).
     *
     * <p>
     * <b>Attention</b> This method must only be used after all ConfigSources got registered and it also must not be
     * used to determine the ProjectStage itself.</p>
     *
     * @param key
     * @param property     the property to look up first and use as the parameter for the main lookup
     * @param defaultValue fallback value
     *
     * @return the configured value or if non found the defaultValue
     *
     */
    public static String getPropertyAwarePropertyValue(String key, String property, String defaultValue)
    {
        String value = getPropertyAwarePropertyValue(key, property);

        return fallbackToDefaultIfEmpty(key, value, defaultValue);
    }

    /**
     * Resolve all values for the given key.
     *
     * @param key
     *
     * @return a List of all found property values, sorted by their ordinal in ascending order
     *
     * @see org.apache.deltaspike.core.spi.config.ConfigSource#getOrdinal()
     */
    public static List<String> getAllPropertyValues(String key)
    {
        // must use a new list because Arrays.asList() is resistant to sorting on some JVMs:
        List<ConfigSource> appConfigSources = sortAscending(new ArrayList<ConfigSource>(
                Arrays.<ConfigSource> asList(getConfigSources())));
        List<String> result = new ArrayList<String>();

        for (ConfigSource configSource : appConfigSources)
        {
            String value = configSource.getPropertyValue(key);

            if (value != null)
            {
                value = filterConfigValue(key, value);
                if (!result.contains(value))
                {
                    result.add(value);
                }
            }
        }

        return result;
    }

    /**
     * Returns a Map of all properties from all scannable config sources. The values of the properties reflect the
     * values that would be obtained by a call to {@link #getPropertyValue(java.lang.String)}, that is, the value of the
     * property from the ConfigSource with the highest ordinal.
     *
     * @see ConfigSource#isScannable()
     */
    public static Map<String, String> getAllProperties()
    {
        // must use a new list because Arrays.asList() is resistant to sorting on some JVMs:
        List<ConfigSource> appConfigSources = sortAscending(new ArrayList<ConfigSource>(
                Arrays.<ConfigSource> asList(getConfigSources())));
        Map<String, String> result = new HashMap<String, String>();

        for (ConfigSource configSource : appConfigSources)
        {
            if (configSource.isScannable())
            {
                result.putAll(configSource.getProperties());
            }
        }

        return Collections.unmodifiableMap(result);
    }

    public static synchronized ConfigSource[] getConfigSources()
    {
        ClassLoader currentClassLoader = ClassUtils.getClassLoader(null);

        ConfigSource[] appConfigSources = configSources.get(currentClassLoader);

        if (appConfigSources == null)
        {
            appConfigSources = sortDescending(resolveConfigSources());

            if (LOG.isLoggable(Level.FINE))
            {
                for (ConfigSource cs : appConfigSources)
                {
                    LOG.log(Level.FINE, "Adding ordinal {0} ConfigSource {1}",
                            new Object[]{cs.getOrdinal(), cs.getConfigName()});
                }
            }

            configSources.put(currentClassLoader, appConfigSources);
        }

        return appConfigSources;
    }

    private static List<ConfigSource> resolveConfigSources()
    {
        List<ConfigSource> appConfigSources = ServiceUtils.loadServiceImplementations(ConfigSource.class);

        List<ConfigSourceProvider> configSourceProviderServiceLoader =
            ServiceUtils.loadServiceImplementations(ConfigSourceProvider.class);

        for (ConfigSourceProvider configSourceProvider : configSourceProviderServiceLoader)
        {
            appConfigSources.addAll(configSourceProvider.getConfigSources());
        }

        List<? extends ConfigFilter> configFilters = ServiceUtils.loadServiceImplementations(ConfigFilter.class);
        for (ConfigFilter configFilter : configFilters)
        {
            addConfigFilter(configFilter);
        }

        return appConfigSources;
    }

    private static ConfigSource[] sortDescending(List<ConfigSource> configSources)
    {
        Collections.sort(configSources, new Comparator<ConfigSource>()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public int compare(ConfigSource configSource1, ConfigSource configSource2)
            {
                return (configSource1.getOrdinal() > configSource2.getOrdinal()) ? -1 : 1;
            }
        });
        return configSources.toArray(new ConfigSource[configSources.size()]);
    }

    private static List<ConfigSource> sortAscending(List<ConfigSource> configSources)
    {
        Collections.sort(configSources, new Comparator<ConfigSource>()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public int compare(ConfigSource configSource1, ConfigSource configSource2)
            {
                return (configSource1.getOrdinal() > configSource2.getOrdinal()) ? 1 : -1;
            }
        });
        return configSources;
    }

    private static ProjectStage getProjectStage()
    {
        if (projectStage == null)
        {
            synchronized (ConfigResolver.class)
            {
                projectStage = ProjectStageProducer.getInstance().getProjectStage();
            }
        }

        return projectStage;
    }

    private static <T> T fallbackToDefaultIfEmpty(String key, T value, T defaultValue)
    {
        if (value == null || (value instanceof String && ((String)value).isEmpty()))
        {
            LOG.log(Level.FINE, "no configured value found for key {0}, using default value {1}.",
                    new Object[]{key, defaultValue});

            return defaultValue;
        }

        return value;
    }

    /**
     * Filter the configured value.
     * This can e.g. be used for decryption.
     * @return the filtered value
     */
    public static String filterConfigValue(String key, String value)
    {
        List<ConfigFilter> currentConfigFilters = getInternalConfigFilters();

        String filteredValue = value;

        for (ConfigFilter filter : currentConfigFilters)
        {
            filteredValue = filter.filterValue(key, filteredValue);
        }
        return filteredValue;
    }

    /**
     * Filter the configured value for logging.
     * This can e.g. be used for displaying ***** instead of a real password.
     * @return the filtered value
     */
    public static String filterConfigValueForLog(String key, String value)
    {
        List<ConfigFilter> currentConfigFilters = getInternalConfigFilters();

        String logValue = value;

        for (ConfigFilter filter : currentConfigFilters)
        {
            logValue = filter.filterValueForLog(key, logValue);
        }
        return logValue;
    }

    public interface Converter<T>
    {

        T convert(String value);

    }

    public interface TypedResolver<T>
    {

        TypedResolver<T> parameterizedBy(String propertyName);

        TypedResolver<T> withCurrentProjectStage(boolean with);

        TypedResolver<T> strictly(boolean strictly);

        TypedResolver<T> withDefault(T value);

        TypedResolver<T> withStringDefault(String value);

        T getValue();

        String getKey();

        String getResolvedKey();

        T getDefaultValue();

    }

    public interface UntypedResolver<T> extends TypedResolver<T>
    {

        <N> TypedResolver<N> as(Class<N> clazz);

        <N> TypedResolver<N> as(Class<N> clazz, Converter<N> converter);

    }

    public static UntypedResolver<String> resolve(String name)
    {
        return new PropertyBuilder<String>(name);
    }

    private static class PropertyBuilder<T> implements UntypedResolver<T>
    {

        private String keyOriginal;

        private String keyResolved;

        private Class<?> configEntryType = String.class;

        private T defaultValue;

        private boolean projectStageAware = true;

        private String propertyParameter;

        private String parameterValue;

        private boolean strictly = false;

        private Converter<?> converter;


        private PropertyBuilder()
        {
        }

        protected PropertyBuilder(String propertyName)
        {
            this.keyOriginal = propertyName;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <N> TypedResolver<N> as(Class<N> clazz)
        {
            configEntryType = clazz;
            return (TypedResolver<N>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <N> TypedResolver<N> as(Class<N> clazz, Converter<N> converter)
        {
            configEntryType = clazz;
            this.converter = converter;

            return (TypedResolver<N>) this;
        }

        @Override
        public TypedResolver<T> withDefault(T value)
        {
            defaultValue = value;
            return this;
        }

        @Override
        public TypedResolver<T> withStringDefault(String value)
        {
            if (value == null || value.isEmpty())
            {
                throw new RuntimeException("Empty String or null supplied as string-default value for property "
                        + keyOriginal);
            }

            defaultValue = convert(value);
            return this;
        }

        @Override
        public TypedResolver<T> parameterizedBy(String propertyName)
        {
            this.propertyParameter = propertyName;

            if (propertyParameter != null && !propertyParameter.isEmpty())
            {
                String parameterValue = ConfigResolver
                        .resolve(propertyParameter)
                        .withCurrentProjectStage(projectStageAware)
                        .getValue();

                if (parameterValue != null && !parameterValue.isEmpty())
                {
                    this.parameterValue = parameterValue;
                }
            }

            return this;
        }

        @Override
        public TypedResolver<T> withCurrentProjectStage(boolean with)
        {
            this.projectStageAware = with;
            return this;
        }

        @Override
        public TypedResolver<T> strictly(boolean strictly)
        {
            this.strictly = strictly;
            return this;
        }

        @Override
        public T getValue()
        {
            String valueStr = resolveStringValue();
            T value = convert(valueStr);

            return fallbackToDefaultIfEmpty(keyResolved, value, defaultValue);
        }

        @Override
        public String getKey()
        {
            return keyOriginal;
        }

        @Override
        public String getResolvedKey()
        {
            return keyResolved;
        }

        @Override
        public T getDefaultValue()
        {
            return defaultValue;
        }

        /**
         * Performs the resolution cascade
         */
        private String resolveStringValue()
        {
            ProjectStage ps = null;
            String value = null;
            keyResolved = keyOriginal;
            int keySuffices = 0;

            // make the longest key
            // first, try appending resolved parameter
            if (propertyParameter != null && !propertyParameter.isEmpty())
            {
                if (parameterValue != null && !parameterValue.isEmpty())
                {
                    keyResolved += "." + parameterValue;
                    keySuffices++;
                }
                // if parameter value can't be resolved and strictly
                else if (strictly)
                {
                    return null;
                }
            }

            // try appending projectstage
            if (projectStageAware)
            {
                ps = getProjectStage();
                keyResolved += "." + ps;
                keySuffices++;
            }

            // make initial resolution of longest key
            value = getPropertyValue(keyResolved);

            // try fallbacks if not strictly
            if (value == null && !strictly)
            {

                // by the length of the longest resolved key already tried
                // breaks are left out intentionally
                switch (keySuffices)
                {

                    case 2:
                        // try base.param
                        keyResolved = keyOriginal + "." + parameterValue;
                        value = getPropertyValue(keyResolved);

                        if (value != null)
                        {
                            return value;
                        }

                        // try base.ps
                        ps = getProjectStage();
                        keyResolved = keyOriginal + "." + ps;
                        value = getPropertyValue(keyResolved);

                        if (value != null)
                        {
                            return value;
                        }

                    case 1:
                        // try base
                        keyResolved = keyOriginal;
                        value = getPropertyValue(keyResolved);
                        return value;

                    default:
                        // the longest key was the base, no fallback
                        return null;
                }
            }

            return value;
        }

        private T convert(String value)
        {

            if (value == null)
            {
                return null;
            }

            Object result = null;

            if (this.converter != null)
            {
                try
                {
                    result = converter.convert(value);
                }
                catch (Exception e)
                {
                    throw ExceptionUtils.throwAsRuntimeException(e);
                }
            }
            else if (String.class.equals(configEntryType))
            {
                result = value;
            }
            else if (Class.class.equals(configEntryType))
            {
                result = ClassUtils.tryToLoadClassForName(value);
            }
            else if (Boolean.class.equals(configEntryType))
            {
                Boolean isTrue = "TRUE".equalsIgnoreCase(value);
                isTrue |= "1".equalsIgnoreCase(value);
                isTrue |= "YES".equalsIgnoreCase(value);
                isTrue |= "Y".equalsIgnoreCase(value);
                isTrue |= "JA".equalsIgnoreCase(value);
                isTrue |= "J".equalsIgnoreCase(value);
                isTrue |= "OUI".equalsIgnoreCase(value);

                result = isTrue;
            }
            else if (Integer.class.equals(configEntryType))
            {
                result = Integer.parseInt(value);
            }
            else if (Long.class.equals(configEntryType))
            {
                result = Long.parseLong(value);
            }
            else if (Float.class.equals(configEntryType))
            {
                result = Float.parseFloat(value);
            }
            else if (Double.class.equals(configEntryType))
            {
                result = Double.parseDouble(value);
            }

            return (T) result;
        }

    }

}
