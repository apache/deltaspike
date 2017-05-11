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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.deltaspike.core.api.crypto.CipherException;
import org.apache.deltaspike.core.api.crypto.CipherService;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.PropertyFileUtils;


/**
 * handle Encryption
 */
@ApplicationScoped
public class DefaultCipherService implements CipherService
{
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    public void setMasterHash(String masterPassword, String masterSalt, boolean overwrite)
        throws IOException, CipherException
    {
        File masterFile = getMasterFile();
        if (!masterFile.getParentFile().exists())
        {
            if (!masterFile.mkdirs())
            {
                throw new IOException("Can not create directory " + masterFile.getParent());
            }
        }
        String saltHash = byteToHex(secureHash(masterSalt));
        String saltKey = byteToHex(secureHash(saltHash));

        String encrypted = byteToHex(aesEncrypt(byteToHex(secureHash(masterPassword)), saltHash));


        Properties keys = new Properties();
        if (masterFile.exists())
        {
            keys = PropertyFileUtils.loadProperties(masterFile.toURI().toURL());
        }

        if (keys.get(saltKey) != null && !overwrite)
        {
            throw new CipherException("MasterKey for hash " + saltHash +
                " already exists. Forced overwrite option needed");
        }

        keys.put(saltKey, encrypted);

        keys.store(new FileOutputStream(masterFile), null);
    }

    protected String getMasterKey(String masterSalt) throws CipherException
    {
        File masterFile = getMasterFile();
        if (!masterFile.exists())
        {
            throw new CipherException("Could not find master.hash file. Create a master password first!");
        }

        try
        {
            String saltHash = byteToHex(secureHash(masterSalt));
            String saltKey = byteToHex(secureHash(saltHash));

            Properties keys = PropertyFileUtils.loadProperties(masterFile.toURI().toURL());

            String encryptedMasterKey = (String) keys.get(saltKey);
            if (encryptedMasterKey == null)
            {
                throw new CipherException("Could not find master key for hash " + saltKey +
                    ". Create a master password first!");
            }

            return aesDecrypt(hexToByte(encryptedMasterKey), saltHash);
        }
        catch (MalformedURLException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    @Override
    public String encrypt(String cleartext, String masterSalt) throws CipherException
    {
        return byteToHex(aesEncrypt(cleartext, getMasterKey(masterSalt)));
    }

    @Override
    public String decrypt(String encryptedValue, String masterSalt) throws CipherException
    {
        return aesDecrypt(hexToByte(encryptedValue), getMasterKey(masterSalt));
    }

    protected File getMasterFile()
    {
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isEmpty())
        {
            throw new IllegalStateException("Can not determine user home directory");
        }
        return new File(userHome, ".deltaspike/master.hash");
    }


    protected byte[] secureHash(String value)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(value.getBytes(UTF_8));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    /**
     * performs an AES encryption of the given text with the given password key
     */
    public byte[] aesEncrypt(String valueToEncrypt, String key)
    {
        try
        {
            SecretKeySpec secretKeySpec = getSecretKeySpec(key);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return cipher.doFinal(valueToEncrypt.getBytes(UTF_8));
        }
        catch (Exception e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    /**
     * performs an AES decryption of the given text with the given key key
     */
    public String aesDecrypt(byte[] encryptedValue, String key)
    {
        try
        {
            SecretKeySpec secretKeySpec = getSecretKeySpec(key);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(encryptedValue), UTF_8);
        }
        catch (Exception e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    private SecretKeySpec getSecretKeySpec(String password)
    {
        byte[] pwdHash = secureHash(password);
        byte[] key = Arrays.copyOf(pwdHash, 16); // use only first 128 bit

        return new SecretKeySpec(key, "AES");
    }


    protected String byteToHex(final byte[] hash)
    {
        StringBuilder sb = new StringBuilder(hash.length * 2);

        for (byte b : hash)
        {
            sb.append(Character.forDigit(b >> 4 & 0x0f, 16));
            sb.append(Character.forDigit(b & 0x0f, 16));
        }

        return sb.toString();
    }

    protected byte[] hexToByte(String hexString)
    {
        if (hexString == null || hexString.length() == 0)
        {
            return new byte[0];
        }
        hexString = hexString.trim();

        if (hexString.length() % 2 != 0)
        {
            throw new IllegalArgumentException("not a valid hex string " + hexString);
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length() / 2; i++)
        {
            int val = (Character.digit(hexString.charAt(i * 2), 16) << 4) +
                      (Character.digit(hexString.charAt( (i * 2) + 1), 16));
            bytes[i] = (byte) val;
        }

        return bytes;
    }

}
