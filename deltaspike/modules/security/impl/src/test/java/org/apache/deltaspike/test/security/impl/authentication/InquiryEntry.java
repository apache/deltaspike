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
package org.apache.deltaspike.test.security.impl.authentication;

import javax.enterprise.inject.Typed;

@Typed()
class InquiryEntry
{
    private final String userName;
    private final Inquiry inquiry;

    InquiryEntry(String userName, Inquiry inquiry)
    {
        this.userName = userName;
        this.inquiry = inquiry;
    }

    String getUserName()
    {
        return userName;
    }

    Inquiry getInquiry()
    {
        return inquiry;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        InquiryEntry that = (InquiryEntry) o;

        if (!inquiry.equals(that.inquiry))
        {
            return false;
        }
        if (!userName.equals(that.userName))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = userName.hashCode();
        result = 31 * result + inquiry.hashCode();
        return result;
    }
}
