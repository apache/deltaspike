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

import org.apache.deltaspike.jsf.api.config.view.Page;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal for {@link org.apache.deltaspike.jsf.api.config.view.Page}
 */
//TODO remove null trick once we can merge with default values and the tests pass
public class PageLiteral extends AnnotationLiteral<Page> implements Page
{
    private static final long serialVersionUID = 1582580975876369665L;

    private final String basePath;
    private final String name;
    private final String extension;
    private final NavigationMode navigation;
    private final ViewParameterMode viewParams;

    public PageLiteral(boolean virtual)
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
    }

    public PageLiteral(String basePath,
                       String name,
                       String extension,
                       NavigationMode navigation,
                       ViewParameterMode viewParams)
    {
        this.basePath = basePath;
        this.name = name;
        this.extension = extension;
        this.navigation = navigation;
        this.viewParams = viewParams;
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
        if (!super.equals(o))
        {
            return false;
        }

        PageLiteral that = (PageLiteral) o;

        if (basePath != null ? !basePath.equals(that.basePath) : that.basePath != null)
        {
            return false;
        }
        if (extension != null ? !extension.equals(that.extension) : that.extension != null)
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
        return result;
    }
}
