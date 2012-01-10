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
package org.apache.deltaspike.test.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Some basic utils
 */
public class FileUtils
{
    private FileUtils()
    {
        // prevent instantiation
    }

    /**
     * @param url the target URL
     * @return a file created based on the given URL
     */
    public static File getFileForURL(String url)
    {
        //fix for wls
        if(!url.startsWith("file:/")) {
            url = "file:/" + url;
        }

        url = url.replaceAll("%20", " ");

        try
        {
            return new File( (new URL(url)).getFile());
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
