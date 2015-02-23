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
package org.apache.deltaspike.data.impl.param;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Query;

import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;

/**
 * Base class for parameters.
 */
public abstract class Parameter
{
    private static final Logger LOG = Logger.getLogger(Parameter.class.getName());

    protected Object value;

    protected Object mappedValue = null;

    public Parameter(Object value)
    {
        this.value = value;
    }

    public abstract void apply(Query query);

    public abstract boolean is(String ident);

    public void applyMapper(QueryInOutMapper<?> mapper)
    {
        if (mapper.mapsParameter(value))
        {
            mappedValue = mapper.mapParameter(value);
            LOG.log(Level.FINE, "Converting param {0} to {1}", new Object[] { value, mappedValue });
        }
    }

    public void updateValue(Object newValue)
    {
        if (mappedValue != null)
        {
            mappedValue = newValue;
        }
        else
        {
            value = newValue;
        }
    }

    protected Object queryValue()
    {
        return mappedValue != null ? mappedValue : value;
    }

}
