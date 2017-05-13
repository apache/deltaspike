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

/**
 * Command Line Interface for CipherService
 */
public class CipherCli
{

    private CipherCli()
    {
        // private ct.
    }

    public static void main(String[] args) throws Exception
    {
        // not using any other libs like commons-cli to save dependencies

        if (args.length < 5)
        {
            printHelp();
            System.exit(-1);
        }

        if (!"encode".equals(args[0]))
        {
            printHelp();
            System.exit(-1);
        }

        String masterPwd = null;
        String plaintext = null;
        String masterSalt = null;
        boolean overwrite = false;

        for (int i = 1; i < args.length; i++)
        {
            String arg = args[i];

            if ("-masterPassword".equals(arg) && i < args.length - 1)
            {
                masterPwd = args[++i];
            }
            else if ("-masterSalt".equals(arg) && i < args.length - 1)
            {
                masterSalt = args[++i];
            }
            else if ("-plaintext".equals(arg) && i < args.length - 1)
            {
                plaintext = args[++i];
            }
            else if ("-overwrite".equals(arg))
            {
                overwrite = true;
            }
        }

        DefaultCipherService defaultCipherService = new DefaultCipherService();

        if (masterPwd != null && masterSalt != null)
        {
            String masterSaltHash = defaultCipherService.setMasterHash(masterPwd, masterSalt, overwrite);

            System.out.println("A new master password got set. Hash key is " + masterSaltHash);
        }
        else if (plaintext != null && masterSalt != null)
        {
            String encrypted = defaultCipherService.encrypt(plaintext, masterSalt);
            System.out.println("Encrypted value: " + encrypted);
        }
        else
        {
            printHelp();
            System.exit(-1);
        }
    }

    private static void printHelp()
    {
        StringBuilder usage = new StringBuilder(1024);
        usage.append("To create a master password use:");
        usage.append("\n$> java -jar deltaspike-core-impl.jar encode -masterPassword " +
            "yourMasterPassword -masterSalt someSecretOnlyKnownToYouAndTheApplication");
        usage.append("\n   you can also specify -overwrite to replace an existing masterpassword.");
        usage.append("\n\nFor encrypting a secret with a previously stored masterPassword use:");
        usage.append("\n$> java -jar deltaspike-core-impl.jar encode -plaintext plaintextToEncrypt " +
            "-masterSalt someSecretOnlyKnownToYouAndTheApplication");
        usage.append("\n\nVisit https://deltaspike.apache.org for more information.\n");

        System.out.print(usage.toString());
    }
}
