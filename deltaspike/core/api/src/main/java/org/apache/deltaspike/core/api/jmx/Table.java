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
package org.apache.deltaspike.core.api.jmx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;

/**
 * Allows to expose in JMX a STRING TabularData without having to built it in the MBean.
 *
 * Ensure to register columns before the lines.
 *
 * You just have to type the operation or attribute with this type to expose it as a TabularData.
 */
public class Table
{
    private Collection<String> columns = new ArrayList<String>();
    private Collection<Collection<String>> values = new ArrayList<Collection<String>>();

    public Table withColumns(final Collection<String> names)
    {
        columns.addAll(names);
        return this;
    }

    public Table withColumns(final String... names)
    {
        columns.addAll(asList(names));
        return this;
    }

    public Table withLines(final Collection<Collection<String>> lines)
    {
        for (final Collection<String> line : lines)
        {
            withLine(line);
        }
        return this;
    }

    public Table withLine(final Collection<String> line)
    {
        if (line.size() != columns.size())
        {
            throw new IllegalArgumentException("Please set columns before lines");
        }
        values.add(line);
        return this;
    }

    public Table withLine(final String... line)
    {
        if (line.length != columns.size())
        {
            throw new IllegalArgumentException("Please set columns before lines");
        }
        values.add(asList(line));
        return this;
    }

    public Collection<String> getColumnNames()
    {
        return Collections.unmodifiableCollection(columns);
    }

    public Collection<Collection<String>> getLines()
    {
        return Collections.unmodifiableCollection(values);
    }
}
