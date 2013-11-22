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
package org.apache.deltaspike.data.test.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@SuppressWarnings("serial")
public class TeeId implements Serializable
{

    @Column(nullable = false)
    private Long teeSetId;

    @Column(nullable = false)
    private Long holeId;

    public TeeId()
    {
    }

    public TeeId(long teeSetId, long holeId)
    {
        this.teeSetId = teeSetId;
        this.holeId = holeId;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (holeId ^ (holeId >>> 32));
        result = prime * result + (int) (teeSetId ^ (teeSetId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        TeeId other = (TeeId) obj;
        if (holeId != other.holeId)
        {
            return false;
        }
        if (teeSetId != other.teeSetId)
        {
            return false;
        }
        return true;
    }

    public long getTeeSetId()
    {
        return teeSetId;
    }

    public void setTeeSetId(long teeSetId)
    {
        this.teeSetId = teeSetId;
    }

    public long getHoleId()
    {
        return holeId;
    }

    public void setHoleId(long holeId)
    {
        this.holeId = holeId;
    }
}
