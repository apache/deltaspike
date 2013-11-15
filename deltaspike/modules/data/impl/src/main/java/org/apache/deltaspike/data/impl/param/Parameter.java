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

    protected final Object value;
    protected final int argIndex;

    protected Object mappedValue = null;

    public Parameter(Object value, int argIndex)
    {
        this.value = value;
        this.argIndex = argIndex;
    }

    public abstract void apply(Query query);

    public void applyMapper(QueryInOutMapper<?> mapper)
    {
        if (mapper.mapsParameter(value))
        {
            mappedValue = mapper.mapParameter(value);
            LOG.log(Level.FINE, "Converting param {0} to {1}", new Object[] { value, mappedValue });
        }
    }

    protected Object queryValue()
    {
        return mappedValue != null ? mappedValue : value;
    }

}
