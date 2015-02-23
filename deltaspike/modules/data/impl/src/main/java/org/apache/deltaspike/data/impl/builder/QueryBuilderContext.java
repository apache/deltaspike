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
package org.apache.deltaspike.data.impl.builder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.deltaspike.data.impl.param.ParameterUpdate;

public class QueryBuilderContext
{

    private final StringBuilder builder;
    private final List<ParameterUpdate> paramUpdates;
    private int counter = 1;

    public QueryBuilderContext()
    {
        this.builder = new StringBuilder();
        this.paramUpdates = new LinkedList<ParameterUpdate>();
    }

    public int increment()
    {
        return counter++;
    }

    public QueryBuilderContext append(String string)
    {
        builder.append(string);
        return this;
    }

    public String resultString()
    {
        return builder.toString();
    }

    public int getCounter()
    {
        return counter;
    }

    public void addParameterUpdate(ParameterUpdate update)
    {
        paramUpdates.add(update);
    }

    public List<ParameterUpdate> getParameterUpdates()
    {
        return Collections.unmodifiableList(paramUpdates);
    }

}
