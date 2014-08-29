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
package org.apache.deltaspike.data.impl.criteria.selection.strings;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SingularAttribute;

public class SubstringFromTo<P> extends SubstringFrom<P>
{

    private final int length;

    public SubstringFromTo(SingularAttribute<? super P, String> attribute, int from, int length)
    {
        super(attribute, from);
        this.length = length;
    }

    @Override
    public <R> Selection<String> toSelection(CriteriaQuery<R> query, CriteriaBuilder builder, Path<? extends P> path)
    {
        return builder.substring(path.get(getAttribute()), getFrom(), length);
    }

}
