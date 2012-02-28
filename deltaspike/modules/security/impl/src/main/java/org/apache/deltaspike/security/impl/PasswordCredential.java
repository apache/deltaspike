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
package org.apache.deltaspike.security.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.deltaspike.security.api.Credential;
import org.apache.deltaspike.security.api.CredentialType;

/**
 * A credential that represents a plain text password.
 */
public class PasswordCredential implements Credential 
{
    public static final SimpleCredentialType TYPE  = new SimpleCredentialType("PASSWORD");
    
    private final String value;
       
    public PasswordCredential(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
    
    public CredentialType getType() 
    {
        return TYPE;
    }

    public Object getEncodedValue()
    {
        if (value != null)
        {
            return md5AsHexString(getValue());
        }
        return null;
    }
    
    /**
     * Computes an md5 hash of a string.
     *
     * @param text the hashed string
     * @return the string hash
     * @throws NullPointerException if text is null
     */
    public static byte[] md5(String text)
    {
        // arguments check
        if (text == null)
        {
            throw new NullPointerException("null text");
        }

        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes());
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Cannot find MD5 algorithm");
        }
    }

    /**
     * Computes an md5 hash and returns the result as a string in hexadecimal format.
     *
     * @param text the hashed string
     * @return the string hash
     * @throws NullPointerException if text is null
     */
    public static String md5AsHexString(String text)
    {
        return toHexString(md5(text));
    }

    /**
     * Returns a string in the hexadecimal format.
     *
     * @param bytes the converted bytes
     * @return the hexadecimal string representing the bytes data
     * @throws IllegalArgumentException if the byte array is null
     */
    public static String toHexString(byte[] bytes)
    {
        if (bytes == null)
        {
            throw new IllegalArgumentException("byte array must not be null");
        }
        
        StringBuffer hex = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++)
        {
            hex.append(Character.forDigit((bytes[i] & 0XF0) >> 4, 16));
            hex.append(Character.forDigit((bytes[i] & 0X0F), 16));
        }
        
        return hex.toString();
    }    
}
