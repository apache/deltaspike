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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Typed;

import org.apache.deltaspike.core.spi.config.ConfigFilter;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.util.ClassUtils;

/**
 * The main entry point to the DeltaSpike configuration mechanism.
 *
 * <p>
 * Resolves configured values of properties by going through the list of configured {@link ConfigSource}s and using the
 * one with the highest ordinal. If multiple {@link ConfigSource}s have the same ordinal, their order is undefined.</p>
 *
 * <p>
 * You can provide your own lookup paths by implementing and registering additional {@link PropertyFileConfig} or
 * {@link ConfigSource} or {@link org.apache.deltaspike.core.spi.config.ConfigSourceProvider} implementations.</p>
 *
 * <p>
 * The resolved configuration is also accessible by simple injection using the {@link ConfigProperty} qualifier.</p>
 *
 * @see <a href="http://deltaspike.apache.org/documentation/configuration.html">DeltaSpike Configuration Mechanism</a>
 */
@Typed()
public final class ConfigResolver
{
    /**
     * Can be used to tweak the application name.
     * This will e.g. used in the JMX MBean to differentiate between applications.
     */
    public static final String DELTASPIKE_APP_NAME_CONFIG = "deltaspike.application.name";

    /**
     * Set this to true if your application should log the whole ConfigSources and Configuration
     * at startup.
     */
    public static final String DELTASPIKE_LOG_CONFIG = "deltaspike.config.log";

    private static ConfigProvider configProvider;

    private ConfigResolver()
    {
        // this is a utility class which doesn't get instantiated.
    }

    public static Config getConfig()
    {
        ClassLoader cl = ClassUtils.getClassLoader(null);
        return getConfig(cl);
    }

    public static Config getConfig(ClassLoader cl)
    {
        return getConfigProvider().getConfig(cl);
    }

    /**
     * This method can be used for programmatically adding {@link ConfigSource}s.
     * It is not needed for normal 'usage' by end users, but only for Extension Developers!
     *
     * @param configSourcesToAdd the ConfigSources to add
     */
    public static synchronized void addConfigSources(List<ConfigSource> configSourcesToAdd)
    {
        getConfigProvider().getConfig().addConfigSources(configSourcesToAdd);
    }

    /**
     * Clear all ConfigSources for the current ClassLoader.
     * This will also clean up all ConfigFilters.
     */
    public static synchronized void freeConfigSources()
    {
        if (configProvider != null)
        {
            ClassLoader cl = ClassUtils.getClassLoader(null);
            configProvider.releaseConfig(cl);
        }
    }

    /**
     * Add a {@link ConfigFilter} to the ConfigResolver. This will only affect the current WebApp (or more precisely the
     * current ClassLoader and it's children).
     *
     * @param configFilter
     */
    public static void addConfigFilter(ConfigFilter configFilter)
    {
        getConfigProvider().getConfig().addConfigFilter(configFilter);
    }

    /**
     * @return the {@link ConfigFilter}s for the current application.
     */
    public static List<ConfigFilter> getConfigFilters()
    {
        return getConfigProvider().getConfig().getConfigFilters();
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
        return getPropertyValue(key, defaultValue, true);
    }


    public static String getPropertyValue(String key, String defaultValue, boolean evaluateVariables)
    {
        return getConfigProvider().getConfig().resolve(key)
                .withDefault(defaultValue)
                .evaluateVariables(evaluateVariables)
                .withCurrentProjectStage(false)
                .getValue();
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
        return getConfigProvider().getConfig().resolve(key)
                .evaluateVariables(true)
                .withCurrentProjectStage(false)
                .getValue();
    }

    /**
     * Resolves the value configured for the given key.
     *
     * @param key the property key
     * @param evaluateVariables whether to evaluate any '${variablename}' variable expressions
     *
     * @return the configured property value from the {@link ConfigSource} with the highest ordinal or null if there is
     *         no configured value for it
     */
    public static String getPropertyValue(String key, boolean evaluateVariables)
    {
        return getConfigProvider().getConfig().resolve(key)
                .evaluateVariables(evaluateVariables)
                .withCurrentProjectStage(false)
                .getValue();
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
        return getConfigProvider().getConfig().resolve(key)
                .withCurrentProjectStage(true)
                .evaluateVariables(true)
                .getValue();
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
        return getConfigProvider().getConfig().resolve(key)
                .withCurrentProjectStage(true)
                .withDefault(defaultValue)
                .evaluateVariables(true)
                .getValue();
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
        return getConfigProvider().getConfig().resolve(key)
                .withCurrentProjectStage(true)
                .parameterizedBy(property)
                .evaluateVariables(true)
                .getValue();
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
        return getConfigProvider().getConfig().resolve(key)
                .withCurrentProjectStage(true)
                .parameterizedBy(property)
                .withDefault(defaultValue)
                .evaluateVariables(true)
                .getValue();
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
        ConfigSource[] configSources = getConfigProvider().getConfig().getConfigSources();
        List<String> result = new ArrayList<String>();
        for (int i = configSources.length; i > 0; i--)
        {
            String value = configSources[i - 1].getPropertyValue(key);

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
        ConfigSource[] configSources = getConfigProvider().getConfig().getConfigSources();
        Map<String, String> result = new HashMap<String, String>();

        for (int i = configSources.length; i > 0; i--)
        {
            ConfigSource configSource = configSources[i - 1];

            if (configSource.isScannable())
            {
                result.putAll(configSource.getProperties());
            }
        }

        return Collections.unmodifiableMap(result);
    }

    public static ConfigSource[] getConfigSources()
    {
        return getConfigProvider().getConfig().getConfigSources();
    }

    /**
     * Filter the configured value.
     * This can e.g. be used for decryption.
     * @return the filtered value
     */
    public static String filterConfigValue(String key, String value)
    {
        return getConfigProvider().getConfig().filterConfigValue(key, value, false);
    }

    /**
     * Filter the configured value for logging.
     * This can e.g. be used for displaying ***** instead of a real password.
     * @return the filtered value
     */
    public static String filterConfigValueForLog(String key, String value)
    {
        return getConfigProvider().getConfig().filterConfigValue(key, value, true);
    }

    /**
     * A very simple interface for conversion of configuration values from String to any Java type.
     *
     * <p>If a Converter implements the {@link java.lang.AutoCloseable} interface it will automatically
     * be released when the Config is shut down.</p>
     * @param <T> The target type of the configuration entry
     */
    public interface Converter<T>
    {

        /**
         * Returns the converted value of the configuration entry.
         * @param value The String property value to convert
         * @return Converted value
         */
        T convert(String value);
    }

    /**
     * A builder-based typed resolution mechanism for configuration values.
     * @param <T> The target type of the configuration entry.
     */
    public interface TypedResolver<T>
    {

        /**
         * Declare the Resolver to return a List of the given Type.
         * When getting value it will be split on each comma (',') character.
         * If a comma is contained in the values it must get escaped with a preceding backslash (&quot;\,&quot;).
         * Any backslash needs to get escaped via double-backslash (&quot;\\&quot;).
         * Note that in property files this leads to &quot;\\\\&quot; as properties escape themselves.
         *
         * @return a TypedResolver for a list of configured comma separated values
         *
         * @since 1.8.0
         */
        TypedResolver<List<T>> asList();

        /**
         * Appends the resolved value of the given property to the key of this builder. This is described in more detail
         * in {@link ConfigResolver#getPropertyAwarePropertyValue(String, String)}.
         * @param propertyName The name of the parameter property
         * @return This builder
         */
        TypedResolver<T> parameterizedBy(String propertyName);

        /**
         * Indicates whether to append the name of the current project stage to the key of this builder. This
         * is described in more detail in {@link ConfigResolver#getProjectStageAwarePropertyValue(String)}. True by
         * default.
         * @param with
         * @return This builder
         */
        TypedResolver<T> withCurrentProjectStage(boolean with);

        /**
         * Indicates whether the fallback resolution sequence should be performed, as described in
         * {@link ConfigResolver#getPropertyAwarePropertyValue(String, String)}. This applies only when
         * {@link #parameterizedBy(String)} or {@link #withCurrentProjectStage(boolean)} is used.
         * @param strictly
         * @return This builder
         */
        TypedResolver<T> strictly(boolean strictly);

        /**
         * Sets the default value to use in case the resolution returns null.
         * @param value the default value
         * @return This builder
         */
        TypedResolver<T> withDefault(T value);

        /**
         * Sets the default value to use in case the resolution returns null. Converts the given String to the type of
         * this resolver using the same method as used for the configuration entries.
         * @param value string value to be converted and used as default
         * @return This builder
         */
        TypedResolver<T> withStringDefault(String value);

        /**
         * Specify that a resolved value will get cached for a certain amount of time.
         * After the time expires the next {@link #getValue()} will again resolve the value
         * from the underlying {@link ConfigResolver}.
         *
         * @param timeUnit the TimeUnit for the value
         * @param value the amount of the TimeUnit to wait
         * @return This builder
         */
        TypedResolver<T> cacheFor(TimeUnit timeUnit, long value);

        /**
         * Whether to evaluate variables in configured values.
         * A variable starts with '${' and ends with '}', e.g.
         * <pre>
         * mycompany.some.url=${myserver.host}/some/path
         * myserver.host=http://localhost:8081
         * </pre>
         * If 'evaluateVariables' is enabled, the result for the above key
         * {@code "mycompany.some.url"} would be:
         * {@code "http://localhost:8081/some/path"}
         * @param evaluateVariables whether to evaluate variables in values or not
         * @return This builder
         */
        TypedResolver<T> evaluateVariables(boolean evaluateVariables);

        /**
         * Whether to log picking up any value changes as INFO.
         *
         * @return This builder
         */
        TypedResolver<T> logChanges(boolean logChanges);

        /**
         * A user can register a Callback which gets notified whenever
         * a config change got detected.
         * The check is performed on every call to {@link #getValue()}
         * and also inside {@link Config#snapshotFor(TypedResolver...)}.
         *
         * If a change got detected the {@param valueChangedCallback} will
         * get invoked in a synchronous way before the {@link #getValue()}
         * or {@link Config#snapshotFor(TypedResolver...)} returns.
         *
         * There can only be a single valueChangedCallback.
         * Using this method multiple times will replace the previously set callback.
         *
         * @param valueChangedCallback a lambda or implementation which will get invoked
         *                             whenever a value change is being detected.
         * @return This builder
         */
        TypedResolver<T> onChange(ConfigChanged<T> valueChangedCallback);


        /**
         * Returns the converted resolved filtered value.
         * @return the resolved value
         */
        T getValue();

        /**
         * Returns the value from a previously taken {@link ConfigSnapshot}.
         *
         * @return the resolved Value
         * @see Config#snapshotFor(TypedResolver[])
         * @throws IllegalArgumentException if the {@link ConfigSnapshot} hasn't been resolved
         *          for this {@link TypedResolver}
         */
        T getValue(ConfigSnapshot configSnapshot);

        /**
         * Returns the key given in {@link #resolve(String)}.
         * @return the original key
         */
        String getKey();

        /**
         * Returns the actual key which led to successful resolution and corresponds to the resolved value. This applies
         * only when {@link #parameterizedBy(String)} or {@link #withCurrentProjectStage(boolean)} is used and
         * {@link #strictly(boolean)} is not used, otherwise the resolved key should always be equal to the original
         * key. This method is provided for cases, when projectStage-aware and/or parameterized resolution is
         * requested but the value for such appended key is not found and some of the fallback keys is used, as
         * described in {@link ConfigResolver#getPropertyAwarePropertyValue(String, String)}.
         * This should be called only after calling {@link #getValue()} otherwise the value is undefined (but likely
         * null).
         * @return
         */
        String getResolvedKey();

        /**
         * Returns the default value provided by {@link #withDefault(Object)} or {@link #withStringDefault(String)}.
         * Returns null if no default was provided.
         * @return the default value or null
         */
        T getDefaultValue();

    }

    /**
     * A builder-based optionally typed resolution mechanism for configuration values.
     * @param <T> This type variable should always be String for UntypedResolver.
     */
    public interface UntypedResolver<T> extends TypedResolver<T>
    {
        /**
         * Sets the type of the configuration entry to the given class and returns this builder as a TypedResolver.
         * Only one of the supported types should be used which includes: Boolean, Class, Integer, Long, Float, Double.
         * For custom types, see {@link #as(Class, Converter)}.
         * @param clazz The target type
         * @param <N> The target type
         * @return This builder as a TypedResolver
         */
        <N> TypedResolver<N> as(Class<N> clazz);

        /**
         * @param type target type, includes List and Map using a Converter
         * @param converter The converter for the target type
         * @param <N> target type
         * @return this builder typed.
         */
        <N> TypedResolver<N> as(Type type, Converter<N> converter);

        /**
         * Sets the type of the configuration entry to the given class, sets the converter to the one given and
         * returns this builder as a TypedResolver. If a converter is provided for one of the types supported by
         * default (see {@link #as(Class)} then the provided converter is used instead of the built-in one.
         * @param clazz The target type
         * @param converter The converter for the target type
         * @param <N> The target type
         * @return This builder as a TypedResolver
         */
        <N> TypedResolver<N> as(Class<N> clazz, Converter<N> converter);

    }

    /**
     * The entry point to the builder-based optionally typed configuration resolution mechanism.
     *
     * String is the default type for configuration entries and is not considered a 'type' by this resolver. Therefore
     * an UntypedResolver is returned by this method. To convert the configuration value to another type, call
     * {@link UntypedResolver#as(Class)}.
     *
     * @param name The property key to resolve
     * @return A builder for configuration resolution.
     */
    public static UntypedResolver<String> resolve(String name)
    {
        return getConfigProvider().getConfig().resolve(name);
    }


    public static ConfigProvider getConfigProvider()
    {
        if (configProvider == null)
        {
            synchronized (ConfigResolver.class)
            {
                if (configProvider == null)
                {
                    Iterator<ConfigProvider> configProviders = ServiceLoader.load(ConfigProvider.class).iterator();
                    if (!configProviders.hasNext())
                    {
                        throw new RuntimeException("Could not load ConfigProvider");
                    }
                    configProvider = configProviders.next();

                    if (configProviders.hasNext())
                    {
                        throw new RuntimeException("Found more than one ConfigProvider");
                    }
                }
            }
        }
        return configProvider;
    }


    /**
     * Provide access to the underlying {@link Config} instance.
     *
     */
    public interface ConfigProvider
    {
        /**
         * Return either an existing Config associated with the current TCCL or a
         * new Config and associate it with the TCCL.
         *
         * @return the Config associated with the current ThreadContextClassLoader
         */
        Config getConfig();

        /**
         * Return either an existing Config associated with the given ClassLoader or a
         * new Config and associate it with the given ClassLoader.
         *
         * @return the Config associated with the given ClassLoader
         */
        Config getConfig(ClassLoader cl);

        /**
         * Release the Config associated with the given ClassLoader.
         * This will also properly close all the ConfigSources, Converters, etc
         * managed by this Config.
         *
         * ATTENTION: Usually this method doesn't need to be invoked manually!
         *   It will automatically get invoked in BeforeShutdown via our ConfigExtension internally.
         */
        void releaseConfig(ClassLoader cl);

        /**
         * Provide access to the ConfigHelper
         */
        ConfigHelper getHelper();
    }

    /**
     * Some utility functions which are useful for implementing own ConfigSources, etc.
     */
    public interface ConfigHelper
    {
        /**
         * @return A Set of all the attributes which differ between the old and new config Map
         *         or an empty Set if there is no difference.
         */
        Set<String> diffConfig(Map<String, String> oldValues, Map<String, String> newValues);
    }

    /**
     * Callback which can be used with {@link TypedResolver#onChange(ConfigChanged)}
     */
    public interface ConfigChanged<T>
    {
        void onValueChange(String key, T oldValue, T newValue);
    }

}
