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
package org.apache.deltaspike.jsf.impl.util;

import org.apache.deltaspike.core.util.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class ViewConfigUtils
{
    public static final String NODE_SEPARATOR = "$";
    private static final String CLASS_PREFIX = "class";
    private static final String INTERFACE_PREFIX = "interface";
    private static final int CLASS_PREFIX_LENGTH = CLASS_PREFIX.length();
    private static final int INTERFACE_PREFIX_LENGTH = INTERFACE_PREFIX.length();

    public static boolean isFolderConfig(Class configClass)
    {
        return Modifier.isAbstract(configClass.getModifiers()) || Modifier.isInterface(configClass.getModifiers());
    }

    //TODO
    public static List<Class> toNodeList(Class nodeClass)
    {
        List<Class> treePath = new ArrayList<Class>();
        String nodeClassName = nodeClass.toString();

        if (nodeClassName.startsWith(CLASS_PREFIX))
        {
            nodeClassName = nodeClassName.substring(CLASS_PREFIX_LENGTH + 1);
        }
        else
        {
            nodeClassName = nodeClassName.substring(INTERFACE_PREFIX_LENGTH + 1);
        }

        treePath.add(nodeClass);

        while (nodeClassName.contains(NODE_SEPARATOR))
        {
            nodeClassName = nodeClassName.substring(0, nodeClassName.lastIndexOf(NODE_SEPARATOR));
            treePath.add(0, ClassUtils.tryToLoadClassForName(nodeClassName));
        }
        return treePath;
    }
}
