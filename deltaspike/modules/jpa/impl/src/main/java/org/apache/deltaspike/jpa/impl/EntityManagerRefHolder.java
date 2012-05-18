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
package org.apache.deltaspike.jpa.impl;

import java.util.List;

//TODO refactor it to remove this workaround
class EntityManagerRefHolder extends EntityManagerRef
{
    private static final long serialVersionUID = -7002376898682639768L;

    private List<EntityManagerRef> entityManagerRefs;

    EntityManagerRefHolder(List<EntityManagerRef> entityManagerRefs)
    {
        super(null, null, null);
        this.entityManagerRefs = entityManagerRefs;
    }

    List<EntityManagerRef> getEntityManagerRefs()
    {
        return entityManagerRefs;
    }
}
