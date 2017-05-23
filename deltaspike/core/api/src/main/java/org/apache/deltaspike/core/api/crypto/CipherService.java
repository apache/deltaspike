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
package org.apache.deltaspike.core.api.crypto;

import java.io.IOException;

/**
 * handle Encryption
 */
public interface CipherService
{

    /**
     * Store a hash based on the given masterpassword and masterSalt in
     * ~/.deltaspike/master.hash
     *
     * @param masterPassword
     * @param masterSalt same masterSalt as later used by the application to decrypt the hash
     * @param overwrite whether an existing passkey file does not get overwritten.
     *
     * @return {@code true} if the master hash got successfully written
     * @throws IOException if the master hash file could not be written
     * @throws IllegalStateException if a masterhash already exists
     */
    void setMasterHash(String masterPassword, String masterSalt, boolean overwrite) throws IOException;

    /**
     * Encrypt the given cleartext.
     * We use the masterSalt to access the MasterHash to use as key for encryption
     *
     * @param cleartext to get encrypted
     * @param masterSalt the same as used for {@link #setMasterHash(String, String, boolean)}
     * @return the encrypted String to store somewhere
     */
    String encrypt(String cleartext, String masterSalt);

    /**
     * Decrypt the given encrypted value.
     * We use the masterSalt to access the MasterHash to use as key for encryption
     *
     * @param encryptedValue to get decrypted
     * @param masterSalt the same as used for {@link #setMasterHash(String, String, boolean)}
     * @return the decrypted plaintext
     */
    String decrypt(String encryptedValue, String masterSalt);

}
