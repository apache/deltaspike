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

import javax.persistence.Query;

/**
 * Query parameters which have an index (?1).
 */
public class IndexedParameter extends Parameter
{

    private final int index;

    public IndexedParameter(int index, Object value)
    {
        super(value);
        this.index = index;
    }

    @Override
    public void apply(Query query)
    {
        query.setParameter(index, queryValue());
    }

    @Override
    public boolean is(String ident)
    {
        try
        {
            return Integer.valueOf(ident).intValue() == index;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

}
