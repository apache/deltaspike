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

package org.apache.deltaspike.core.impl.crypto;

import javax.enterprise.context.ApplicationScoped;

import java.io.IOException;

import org.apache.deltaspike.core.api.crypto.CipherService;

@ApplicationScoped
public class CdiCipherService implements CipherService
{
    private DefaultCipherService cipherService = new DefaultCipherService();


    @Override
    public void setMasterHash(String masterPassword, String masterSalt, boolean overwrite)
        throws IOException
    {
        cipherService.setMasterHash(masterPassword, masterSalt, overwrite);
    }

    @Override
    public String encrypt(String cleartext, String masterSalt)
    {
        return cipherService.encrypt(cleartext, masterSalt);
    }

    @Override
    public String decrypt(String encryptedValue, String masterSalt)
    {
        return cipherService.decrypt(encryptedValue, masterSalt);
    }
}
