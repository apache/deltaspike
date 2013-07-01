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

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.jsf.api.config.view.Folder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class ViewConfigUtils
{
    public static boolean isFolderConfig(Class configClass)
    {
        return configClass != null && (
                (ViewConfig.class.isAssignableFrom(configClass) &&
                        Modifier.isAbstract(configClass.getModifiers()) ||
                        Modifier.isInterface(configClass.getModifiers())
                ) ||
                configClass.isAnnotationPresent(Folder.class));
    }

    //TODO
    public static List<Class> toNodeList(Class nodeClass)
    {
        List<Class> treePath = new ArrayList<Class>();
        while (nodeClass != null)
        {
            treePath.add(0, nodeClass);
            nodeClass = nodeClass.getEnclosingClass();
        }
        return treePath;
    }
}
