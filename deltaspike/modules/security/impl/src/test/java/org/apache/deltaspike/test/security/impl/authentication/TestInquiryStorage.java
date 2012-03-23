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

import org.apache.deltaspike.security.api.Identity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TestInquiryStorage implements InquiryStorage
{
    private Map<String, InquiryEntry> userInquiries = new ConcurrentHashMap<String, InquiryEntry>();

    private Map<String, Inquiry> anonymInquiries = new ConcurrentHashMap<String, Inquiry>();

    @Inject
    private Identity identity;

    public boolean addInquiry(Inquiry inquiry)
    {
        if(identity.isLoggedIn())
        {
            userInquiries.put(inquiry.getInquiryId(), new InquiryEntry(identity.getUser().getId(), inquiry));
        }
        else
        {
            this.anonymInquiries.put(inquiry.getInquiryId(), inquiry);
        }
        return true;
    }
    
    Collection<InquiryEntry> getUserInquiries()
    {
        return this.userInquiries.values();
    }

    Collection<Inquiry> getAnonymInquiries()
    {
        return this.anonymInquiries.values();
    }
}
