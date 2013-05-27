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
package org.apache.deltaspike.core.impl.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Typed;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;

/**
 * This is the internal helper class for low level access to JNDI
 */
@Typed()
public abstract class JndiUtils
{
    private static final Logger LOG = Logger.getLogger(JndiUtils.class.getName());

    private static InitialContext initialContext = null;

    static
    {
        try
        {
            initialContext = new InitialContext();
        }
        catch (Exception e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    private JndiUtils()
    {
        // prevent instantiation
    }

    /**
     * Resolves an instance for the given name.
     *
     * @param name       current name
     * @param targetType target type
     * @param <T>        type
     * @return the found instance, null otherwise
     */
    public static <T> T lookup(Name name, Class<? extends T> targetType)
    {
        try
        {
            return verifyLookupResult(initialContext.lookup(name), name.toString(), targetType);
        }
        catch (NamingException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    /**
     * Resolves an instance for the given name.
     *
     * @param name       current name
     * @param targetType target type
     * @param <T>        type
     * @return the found instance, null otherwise
     */
    public static <T> T lookup(String name, Class<? extends T> targetType)
    {
        try
        {
            return verifyLookupResult(initialContext.lookup(name), name, targetType);
        }
        catch (NamingException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    /**
     * Does a checks on given instance looked up previously from JNDI.
     *
     * @param name       current name
     * @param targetType target type
     * @param <T>        type
     * @return the found instance, null otherwise
     */
    @SuppressWarnings("unchecked")
    private static <T> T verifyLookupResult(Object result, String name, Class<? extends T> targetType)
    {
        if (result != null)
        {
            if (targetType.isAssignableFrom(result.getClass()))
            {
                // we have a value and the type fits
                return (T) result;
            }
            else if (result instanceof String) //but the target type != String
            {
                // lookedUp might be a class name
                try
                {
                    Class<?> classOfResult = ClassUtils.loadClassForName((String) result);
                    if (targetType.isAssignableFrom(classOfResult))
                    {
                        try
                        {
                            return (T) classOfResult.newInstance();
                        }
                        catch (Exception e)
                        {
                            // could not create instance
                            LOG.log(Level.SEVERE, "Class " + classOfResult + " from JNDI lookup for name "
                                    + name + " could not be instantiated", e);
                        }
                    }
                    else
                    {
                        // lookedUpClass does not extend/implement expectedClass
                        LOG.log(Level.SEVERE, "JNDI lookup for key " + name
                                + " returned class " + classOfResult.getName()
                                + " which does not implement/extend the expected class"
                                + targetType.getName());
                    }
                }
                catch (ClassNotFoundException cnfe)
                {
                    // could not find class
                    LOG.log(Level.SEVERE, "Could not find Class " + result
                            + " from JNDI lookup for name " + name, cnfe);
                }
            }
            else
            {
                // we have a value, but the value does not fit
                LOG.log(Level.SEVERE, "JNDI lookup for key " + name + " should return a value of "
                        + targetType + ", but returned " + result);
            }
        }

        return null;
    }

    /**
     * Resolves an instances for the given naming context.
     *
     * @param name       context name
     * @param type       target type
     * @param <T>        type
     * @return the found instances, null otherwise
     */
    public static <T> Map<String, T> list(String name, Class<T> type)
    {
        Map<String, T> result = new HashMap<String, T>();

        try
        {
            NameParser nameParser = initialContext.getNameParser(name);
            NamingEnumeration<NameClassPair> enumeration = initialContext.list(name);
            while (enumeration.hasMoreElements())
            {
                try
                {
                    NameClassPair binding = enumeration.nextElement();
                    Name bindingName = nameParser.parse(name).add(binding.getName());                
                    result.put(binding.getName(), lookup(bindingName, type));
                }
                catch (NamingException e)
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        // this is expected if there is no entry in JNDI for the requested name or type
                        // so finest level is ok, if devs want to see it they can enable this logger level.
                        LOG.log(Level.FINEST, "InitialContext#list failed!", e);
                    }
                }
            }
        }
        catch (NamingException e)
        {
            // this is fine at this point, since the individual lines will be caught currently.
            LOG.log(Level.WARNING,"Problem reading the name of the JNDI location " + name
                + " or failuring listing pairs.",e);
        }
        return result;
    }
}
