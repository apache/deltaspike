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
package org.apache.deltaspike.jsf.api.config.view;

import org.apache.deltaspike.core.api.config.view.metadata.SkipMetaDataMerge;
import org.apache.deltaspike.core.api.config.view.metadata.ViewMetaData;
import org.apache.deltaspike.core.spi.config.view.ConfigPreProcessor;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.jsf.api.config.base.JsfBaseConfig;
import org.apache.deltaspike.jsf.api.literal.FolderLiteral;
import org.apache.deltaspike.jsf.util.NamingConventionUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Iterator;
import java.util.Set;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Optional annotation to specify folder specific meta-data
 */

//don't use @Inherited
@Target(TYPE)
@Retention(RUNTIME)
@Documented

@ViewMetaData(preProcessor = Folder.FolderConfigPreProcessor.class)
public @interface Folder
{
    /**
     * Allows to specify a custom (folder-)name
     *
     * @return name of the folder
     */
    @SkipMetaDataMerge
    String name() default ".";

    /**
     * Allows to add a custom inline path-builder
     * (a custom default implementation can be configured globally via the config mechanism provided by DeltaSpike)
     * @return path builder which allows to customize the naming conventions for the folder-name
     */
    Class<? extends NameBuilder> folderNameBuilder() default DefaultFolderNameBuilder.class;

    static class FolderConfigPreProcessor implements ConfigPreProcessor<Folder>
    {
        @Override
        public Folder beforeAddToConfig(Folder folder, ViewConfigNode viewConfigNode)
        {
            boolean defaultValueReplaced = false;

            /*
             * file name
             */
            NameBuilder folderNameBuilder = getFolderNameBuilder(folder);
            String name = folderNameBuilder.build(folder, viewConfigNode);

            if (folderNameBuilder.isDefaultValueReplaced())
            {
                defaultValueReplaced = true;
            }

            if (defaultValueReplaced)
            {
                Folder result = new FolderLiteral(name, folder.folderNameBuilder());
                updateNodeMetaData(viewConfigNode, result);
                return result;
            }
            return folder;
        }

        private void updateNodeMetaData(ViewConfigNode viewConfigNode, Folder folder)
        {
            Set<Annotation> metaData = viewConfigNode.getMetaData();

            Iterator<? extends Annotation> metaDataIterator = metaData.iterator();

            while (metaDataIterator.hasNext())
            {
                if (Folder.class.equals(metaDataIterator.next().annotationType()))
                {
                    metaDataIterator.remove();
                    break;
                }
            }
            metaData.add(folder);
        }

        private NameBuilder getFolderNameBuilder(Folder folder)
        {
            NameBuilder folderNameBuilder;
            if (DefaultFolderNameBuilder.class.equals(folder.folderNameBuilder()))
            {
                String customDefaultFolderNameBuilderClassName =
                    JsfBaseConfig.ViewConfigCustomization.CUSTOM_DEFAULT_FOLDER_NAME_BUILDER;

                if (customDefaultFolderNameBuilderClassName != null)
                {
                    folderNameBuilder =
                        (NameBuilder) ClassUtils.tryToInstantiateClassForName(customDefaultFolderNameBuilderClassName);
                }
                else
                {
                    folderNameBuilder = new DefaultFolderNameBuilder();
                }
            }
            else
            {
                folderNameBuilder = ClassUtils.tryToInstantiateClass(folder.folderNameBuilder());
            }
            return folderNameBuilder;
        }
    }

    //TODO discuss if we use a central interface in the spi package
    //advantage: can be reused
    //disadvantage: a wrong builder can get assigned more easily, show usages will list more
    interface NameBuilder
    {
        String build(Folder folder, ViewConfigNode viewConfigNode);

        boolean isDefaultValueReplaced();
    }

    class DefaultFolderNameBuilder implements NameBuilder
    {
        private boolean defaultValueReplaced = false;

        @Override
        public String build(Folder folder, ViewConfigNode viewConfigNode)
        {
            String name = folder.name();

            if (name == null /*null used as marker value for dyn. added instances*/ || ".".equals(name) /*default*/)
            {
                name = NamingConventionUtils.toPath(viewConfigNode);

                this.defaultValueReplaced = true;
            }

            if (name != null && name.startsWith("."))
            {
                name = NamingConventionUtils.toPath(viewConfigNode.getParent()) + name.substring(1);

                this.defaultValueReplaced = true;
            }

            if (name != null && !name.startsWith(".") && !name.startsWith("/"))
            {
                name = NamingConventionUtils.toPath(viewConfigNode.getParent()) + name;

                this.defaultValueReplaced = true;
            }

            if (name != null && !name.endsWith("/"))
            {
                name = name + "/";

                this.defaultValueReplaced = true;
            }

            if (name != null && name.contains("//"))
            {
                name = name.replace("//", "/");

                this.defaultValueReplaced = true;
            }

            return name;
        }

        public boolean isDefaultValueReplaced()
        {
            return defaultValueReplaced;
        }
    }
}
