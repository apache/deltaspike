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
package org.apache.deltaspike.jsf.util;

import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.api.config.view.Folder;

import java.lang.annotation.Annotation;

public abstract class NamingConventionUtils
{
    public static String toPath(ViewConfigNode node)
    {
        if (node == null || node.getParent() == null) //root-node
        {
            return "/";
        }

        Folder folderMetaData = null;

        for (Annotation nodeMetaData : node.getMetaData())
        {
            if (Folder.class.isAssignableFrom(nodeMetaData.annotationType()))
            {
                folderMetaData = (Folder)nodeMetaData;
                break;
            }
        }


        String folderName = null;

        if (folderMetaData != null)
        {
            folderName = folderMetaData.name();
        }

        if (".".equals(folderName))
        {
            folderName = null; //default value -> fallback to the default naming
        }

        if (folderName == null)
        {
            folderName = node.getSource().getSimpleName();
            folderName = "./" + folderName.substring(0, 1).toLowerCase() + folderName.substring(1) + "/";
        }

        //@Folder found and no relative path
        if (folderMetaData != null && !folderName.startsWith("."))
        {
            return folderName;
        }
        return toPath(node.getParent()) + folderName.substring(1);
    }
}
