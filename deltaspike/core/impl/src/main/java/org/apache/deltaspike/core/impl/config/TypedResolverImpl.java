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
package org.apache.deltaspike.core.impl.config;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.ConfigSnapshot;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;



public class TypedResolverImpl<T> implements ConfigResolver.UntypedResolver<T>
{
    private static final Logger LOG = Logger.getLogger(TypedResolverImpl.class.getName());

    private final ConfigImpl config;

    private String keyOriginal;

    private String keyResolved;

    private Type configEntryType = String.class;

    private boolean withDefault = false;
    private T defaultValue;

    private boolean projectStageAware = true;

    private String propertyParameter;

    private String parameterValue;

    private boolean strictly = false;

    private boolean isList = false;

    private ConfigResolver.Converter<?> converter;

    private boolean evaluateVariables = false;

    private boolean logChanges = false;
    private ConfigResolver.ConfigChanged<T> valueChangedCallback = null;

    private long cacheTimeMs = -1;

    private volatile long reloadAfter = -1;
    private long lastReloadedAt = -1;

    private T lastValue = null;


    TypedResolverImpl(ConfigImpl config, String propertyName)
    {
        this.config = config;
        this.keyOriginal = propertyName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N> ConfigResolver.TypedResolver<N> as(Class<N> clazz)
    {
        configEntryType = clazz;
        return (ConfigResolver.TypedResolver<N>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigResolver.TypedResolver<List<T>> asList()
    {
        isList = true;
        ConfigResolver.TypedResolver<List<T>> listTypedResolver = (ConfigResolver.TypedResolver<List<T>>) this;

        if (defaultValue == null)
        {
            // the default for lists is an empty list instead of null
            return listTypedResolver.withDefault(Collections.<T>emptyList());
        }

        return listTypedResolver;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N> ConfigResolver.TypedResolver<N> as(Class<N> clazz, ConfigResolver.Converter<N> converter)
    {
        configEntryType = clazz;
        this.converter = converter;

        return (ConfigResolver.TypedResolver<N>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N> ConfigResolver.TypedResolver<N> as(Type clazz, ConfigResolver.Converter<N> converter)
    {
        configEntryType = clazz;
        this.converter = converter;

        return (ConfigResolver.TypedResolver<N>) this;
    }

    @Override
    public ConfigResolver.TypedResolver<T> withDefault(T value)
    {
        defaultValue = value;
        withDefault = true;
        return this;
    }

    @Override
    public ConfigResolver.TypedResolver<T> withStringDefault(String value)
    {
        if (value == null || value.isEmpty())
        {
            throw new RuntimeException("Empty String or null supplied as string-default value for property "
                    + keyOriginal);
        }

        if (isList)
        {
            defaultValue = splitAndConvertListValue(value);
        }
        else
        {
            defaultValue = convert(value);
        }
        withDefault = true;
        return this;
    }

    @Override
    public ConfigResolver.TypedResolver<T> cacheFor(TimeUnit timeUnit, long value)
    {
        this.cacheTimeMs = timeUnit.toMillis(value);
        return this;
    }

    @Override
    public ConfigResolver.TypedResolver<T> parameterizedBy(String propertyName)
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
    public ConfigResolver.TypedResolver<T> withCurrentProjectStage(boolean with)
    {
        this.projectStageAware = with;
        return this;
    }

    @Override
    public ConfigResolver.TypedResolver<T> strictly(boolean strictly)
    {
        this.strictly = strictly;
        return this;
    }

    @Override
    public ConfigResolver.TypedResolver<T> evaluateVariables(boolean evaluateVariables)
    {
        this.evaluateVariables = evaluateVariables;
        return this;
    }

    @Override
    public ConfigResolver.TypedResolver<T> logChanges(boolean logChanges)
    {
        this.logChanges = logChanges;
        return this;
    }

    @Override
    public ConfigResolver.TypedResolver<T> onChange(ConfigResolver.ConfigChanged<T> valueChangedCallback)
    {
        this.valueChangedCallback = valueChangedCallback;
        return this;
    }

    @Override
    public T getValue(ConfigSnapshot snapshot)
    {
        ConfigSnapshotImpl snapshotImpl = (ConfigSnapshotImpl) snapshot;

        if (!snapshotImpl.getConfigValues().containsKey(this))
        {
            throw new IllegalArgumentException("The TypedResolver for key " + getKey() +
                " does not belong the given ConfigSnapshot!");
        }

        return (T) snapshotImpl.getConfigValues().get(this);
    }

    @Override
    public T getValue()
    {
        long now = -1;
        if (cacheTimeMs > 0)
        {
            now = System.nanoTime();
            if (now <= reloadAfter)
            {
                // now check if anything in the underlying Config got changed
                long lastCfgChange = config.getLastChanged();
                if (lastCfgChange < lastReloadedAt)
                {
                    return lastValue;
                }
            }
        }

        String valueStr = resolveStringValue();
        T value;
        if (isList)
        {
            value = splitAndConvertListValue(valueStr);
        }
        else
        {
            value = convert(valueStr);
        }

        if (withDefault)
        {
            ConfigResolverContext configResolverContext = new ConfigResolverContext()
                    .setEvaluateVariables(evaluateVariables)
                    .setProjectStageAware(projectStageAware);
            value = fallbackToDefaultIfEmpty(keyResolved, value, defaultValue, configResolverContext);
            if (isList && String.class.isInstance(value))
            {
                value = splitAndConvertListValue(String.class.cast(value));
            }
        }

        if ((logChanges || valueChangedCallback != null)
            && (value != null && !value.equals(lastValue) || (value == null && lastValue != null)))
        {
            if (logChanges)
            {
                LOG.log(Level.INFO, "New value {0} for key {1}.",
                    new Object[]{ConfigResolver.filterConfigValueForLog(keyOriginal, valueStr), keyOriginal});
            }

            if (valueChangedCallback != null)
            {
                valueChangedCallback.onValueChange(keyOriginal, lastValue, value);
            }
        }

        lastValue = value;

        if (cacheTimeMs > 0)
        {
            reloadAfter = now + TimeUnit.MILLISECONDS.toNanos(cacheTimeMs);
            lastReloadedAt = now;
        }

        return value;
    }

    private T splitAndConvertListValue(String valueStr)
    {
        if (valueStr == null)
        {
            return null;
        }

        List list = new ArrayList();
        StringBuilder currentValue = new StringBuilder();
        int length = valueStr.length();
        for (int i = 0; i < length; i++)
        {
            char c = valueStr.charAt(i);
            if (c == '\\')
            {
                if (i < length - 1)
                {
                    char nextC = valueStr.charAt(i + 1);
                    currentValue.append(nextC);
                    i++;
                }
            }
            else if (c == ',')
            {
                String trimedVal = currentValue.toString().trim();
                if (trimedVal.length() > 0)
                {
                    list.add(convert(trimedVal));
                }

                currentValue.setLength(0);
            }
            else
            {
                currentValue.append(c);
            }
        }

        String trimedVal = currentValue.toString().trim();
        if (trimedVal.length() > 0)
        {
            list.add(convert(trimedVal));
        }

        return (T) list;
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

    /**
     * If a converter was provided for this builder, it takes precedence over the built-in converters.
     */
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

    private <T> T fallbackToDefaultIfEmpty(String key, T value, T defaultValue,
                                           ConfigResolverContext configResolverContext)
    {
        if (value == null || (value instanceof String && ((String)value).isEmpty()))
        {
            if (configResolverContext != null && defaultValue instanceof String
                    && configResolverContext.isEvaluateVariables())
            {
                defaultValue = (T) resolveVariables((String) defaultValue);
            }

            if (LOG.isLoggable(Level.FINE))
            {
                LOG.log(Level.FINE, "no configured value found for key {0}, using default value {1}.",
                        new Object[]{key, defaultValue});
            }

            return defaultValue;
        }

        return value;
    }

    /**
     * recursively resolve any ${varName} in the value
     */
    private String resolveVariables(String value)
    {
        int startVar = 0;
        while ((startVar = value.indexOf("${", startVar)) >= 0)
        {
            int endVar = value.indexOf("}", startVar);
            if (endVar <= 0)
            {
                break;
            }
            String varName = value.substring(startVar + 2, endVar);
            if (varName.isEmpty())
            {
                break;
            }

            String variableValue = new TypedResolverImpl<String>(this.config, varName)
                    .withCurrentProjectStage(this.projectStageAware)
                    .evaluateVariables(true)
                    .getValue();

            if (variableValue != null)
            {
                value = value.replace("${" + varName + "}", variableValue);
            }
            startVar++;
        }
        return value;
    }

    private ProjectStage getProjectStage()
    {
        return ProjectStageProducer.getInstance().getProjectStage();
    }

    private String getPropertyValue(String key)
    {
        String value;
        for (ConfigSource configSource : config.getConfigSources())
        {
            value = configSource.getPropertyValue(key);

            if (value != null)
            {
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.log(Level.FINE, "found value {0} for key {1} in ConfigSource {2}.",
                            new Object[]{config.filterConfigValue(key, value, true),
                                key, configSource.getConfigName()});
                }

                if (this.evaluateVariables)
                {
                    value = resolveVariables(value);
                }

                return config.filterConfigValue(key, value, false);
            }

            if (LOG.isLoggable(Level.FINE))
            {
                LOG.log(Level.FINER, "NO value found for key {0} in ConfigSource {1}.",
                        new Object[]{key, configSource.getConfigName()});
            }
        }

        return null;
    }


}
