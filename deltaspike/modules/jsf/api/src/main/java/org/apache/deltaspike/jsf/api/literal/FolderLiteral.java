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

package org.apache.deltaspike.jsf.api.literal;

import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.jsf.api.config.base.JsfBaseConfig;
import org.apache.deltaspike.jsf.api.config.view.Folder;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal for {@link Folder}
 */
//TODO remove null trick once we can merge with default values and the tests pass
public class FolderLiteral extends AnnotationLiteral<Folder> implements Folder
{
    private static final long serialVersionUID = 2582580975876369665L;

    private final String name;
    private final Class<? extends NameBuilder> folderNameBuilder;

    public FolderLiteral(boolean virtual)
    {
        if (virtual)
        {
            this.name = null;
        }
        else
        {
            this.name = "";
        }

        final String customDefaultFolderNameBuilderClassName =
            JsfBaseConfig.ViewConfigCustomization.CUSTOM_DEFAULT_FOLDER_NAME_BUILDER;

        if (customDefaultFolderNameBuilderClassName != null)
        {
            this.folderNameBuilder = ClassUtils.tryToLoadClassForName(customDefaultFolderNameBuilderClassName);
        }
        else
        {
            this.folderNameBuilder = DefaultFolderNameBuilder.class;
        }
    }

    public FolderLiteral(String name, Class<? extends NameBuilder> folderNameBuilder)
    {
        this.name = name;
        this.folderNameBuilder = folderNameBuilder;
    }

    @Override
    public String name()
    {
        return this.name;
    }

    @Override
    public Class<? extends NameBuilder> folderNameBuilder()
    {
        return this.folderNameBuilder;
    }

    /*
    * generated
    */

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!(o instanceof FolderLiteral))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        FolderLiteral that = (FolderLiteral) o;

        if (!folderNameBuilder.equals(that.folderNameBuilder))
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + folderNameBuilder.hashCode();
        return result;
    }
}
