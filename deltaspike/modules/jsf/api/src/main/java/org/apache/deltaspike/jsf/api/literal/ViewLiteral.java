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
import org.apache.deltaspike.jsf.api.config.view.View;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal for {@link org.apache.deltaspike.jsf.api.config.view.View}
 */
//TODO remove null trick once we can merge with default values and the tests pass
public class ViewLiteral extends AnnotationLiteral<View> implements View
{
    private static final long serialVersionUID = 1582580975876369665L;

    private final String basePath;
    private final String name;
    private final String extension;
    private final NavigationMode navigation;
    private final ViewParameterMode viewParams;

    private final Class<? extends NameBuilder> basePathBuilder;
    private final Class<? extends NameBuilder> fileNameBuilder;
    private final Class<? extends NameBuilder> extensionBuilder;

    public ViewLiteral(boolean virtual)
    {
        if (virtual)
        {
            this.basePath = null;
            this.name = null;
            this.extension = null;
            this.navigation = null;
            this.viewParams = null;
        }
        else
        {
            this.basePath = "";
            this.name = "";
            this.extension = Extension.DEFAULT;
            this.navigation = NavigationMode.FORWARD;
            this.viewParams = ViewParameterMode.DEFAULT;
        }

        final String customDefaultBasePathBuilderClassName =
            JsfBaseConfig.ViewConfigCustomization.CUSTOM_DEFAULT_BASE_PATH_BUILDER;

        if (customDefaultBasePathBuilderClassName != null)
        {
            this.basePathBuilder = ClassUtils.tryToLoadClassForName(customDefaultBasePathBuilderClassName);
        }
        else
        {
            this.basePathBuilder = DefaultBasePathBuilder.class;
        }


        final String customDefaultFileNameBuilderClassName =
            JsfBaseConfig.ViewConfigCustomization.CUSTOM_DEFAULT_FILE_NAME_BUILDER;

        if (customDefaultFileNameBuilderClassName != null)
        {
            this.fileNameBuilder = ClassUtils.tryToLoadClassForName(customDefaultFileNameBuilderClassName);
        }
        else
        {
            this.fileNameBuilder = DefaultFileNameBuilder.class;
        }


        final String customDefaultExtensionBuilderClassName =
            JsfBaseConfig.ViewConfigCustomization.CUSTOM_DEFAULT_EXTENSION_BUILDER;

        if (customDefaultExtensionBuilderClassName != null)
        {
            this.extensionBuilder = ClassUtils.tryToLoadClassForName(customDefaultExtensionBuilderClassName);
        }
        else
        {
            this.extensionBuilder = DefaultExtensionBuilder.class;
        }
    }

    public ViewLiteral(String basePath,
                       String name,
                       String extension,
                       NavigationMode navigation,
                       ViewParameterMode viewParams,
                       Class<? extends NameBuilder> basePathBuilder,
                       Class<? extends NameBuilder> fileNameBuilder,
                       Class<? extends NameBuilder> extensionBuilder)
    {
        this.basePath = basePath;
        this.name = name;
        this.extension = extension;
        this.navigation = navigation;
        this.viewParams = viewParams;
        this.basePathBuilder = basePathBuilder;
        this.fileNameBuilder = fileNameBuilder;
        this.extensionBuilder = extensionBuilder;
    }

    @Override
    public String basePath()
    {
        return this.basePath;
    }

    @Override
    public String name()
    {
        return this.name;
    }

    @Override
    public String extension()
    {
        return this.extension;
    }

    @Override
    public NavigationMode navigation()
    {
        return this.navigation;
    }

    @Override
    public ViewParameterMode viewParams()
    {
        return this.viewParams;
    }

    @Override
    public Class<? extends NameBuilder> basePathBuilder()
    {
        return this.basePathBuilder;
    }

    @Override
    public Class<? extends NameBuilder> fileNameBuilder()
    {
        return this.fileNameBuilder;
    }

    @Override
    public Class<? extends NameBuilder> extensionBuilder()
    {
        return this.extensionBuilder;
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
        if (!(o instanceof ViewLiteral))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        ViewLiteral that = (ViewLiteral) o;

        if (basePath != null ? !basePath.equals(that.basePath) : that.basePath != null)
        {
            return false;
        }
        if (!basePathBuilder.equals(that.basePathBuilder))
        {
            return false;
        }
        if (extension != null ? !extension.equals(that.extension) : that.extension != null)
        {
            return false;
        }
        if (!extensionBuilder.equals(that.extensionBuilder))
        {
            return false;
        }
        if (!fileNameBuilder.equals(that.fileNameBuilder))
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (navigation != that.navigation)
        {
            return false;
        }
        if (viewParams != that.viewParams)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (basePath != null ? basePath.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        result = 31 * result + (navigation != null ? navigation.hashCode() : 0);
        result = 31 * result + (viewParams != null ? viewParams.hashCode() : 0);
        result = 31 * result + basePathBuilder.hashCode();
        result = 31 * result + fileNameBuilder.hashCode();
        result = 31 * result + extensionBuilder.hashCode();
        return result;
    }
}
