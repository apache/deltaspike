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
import org.apache.deltaspike.jsf.api.literal.ViewLiteral;
import org.apache.deltaspike.jsf.util.NamingConventionUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.deltaspike.jsf.api.config.view.View.Extension.XHTML;
import static org.apache.deltaspike.jsf.api.config.view.View.NavigationMode.FORWARD;
import static org.apache.deltaspike.jsf.api.config.view.View.ViewParameterMode.EXCLUDE;

/**
 * Optional annotation to specify page specific meta-data
 */

//don't use @Inherited
@Target(TYPE)
@Retention(RUNTIME)
@Documented

@ViewMetaData(preProcessor = View.ViewConfigPreProcessor.class)
public @interface View
{
    /**
     * Allows to specify a custom base-path for the page represented by the view-config
     * @return base-path
     */
    @SkipMetaDataMerge
    String basePath() default "";

    /**
     * Allows to specify a custom (file-)name for the page represented by the view-config
     *
     * @return name of the page
     */
    @SkipMetaDataMerge
    String name() default "";

    /**
     * Allows to specify the (file-)extension for the page represented by the view-config
     *
     * @return extension of the page
     */
    String extension() default Extension.DEFAULT;

    /**
     * Allows to specify navigation-mode which should be used to navigate to the page represented by the view-config
     *
     * @return navigation-mode which should be used to navigate to the page represented by the view-config
     */
    NavigationMode navigation() default NavigationMode.DEFAULT;

    /**
     * for including view params in jsf2
     *
     * @return the strategy which should be used by jsf2 for handling view-parameters (for the navigation)
     */
    ViewParameterMode viewParams() default ViewParameterMode.DEFAULT;

    /**
     * Allows to add a custom inline path-builder
     * (a custom default implementation can be configured globally via the config mechanism provided by DeltaSpike)
     * @return path builder which allows to customize the naming conventions for the base-path
     */
    Class<? extends NameBuilder> basePathBuilder() default DefaultBasePathBuilder.class;

    /**
     * Allows to add a custom inline path-builder
     * (a custom default implementation can be configured globally via the config mechanism provided by DeltaSpike)
     * @return path builder which allows to customize the naming conventions for the file-name
     */
    Class<? extends NameBuilder> fileNameBuilder() default DefaultFileNameBuilder.class;

    /**
     * Allows to add a custom inline path-builder
     * (a custom default implementation can be configured globally via the config mechanism provided by DeltaSpike)
     * @return path builder which allows to customize the naming conventions for the file-extension
     */
    Class<? extends NameBuilder> extensionBuilder() default DefaultExtensionBuilder.class;

    /**
     * Extension of the markup file
     */
    interface Extension
    {
        String DEFAULT = "";
        String XHTML = "xhtml";
        String JSF = "jsf";
        String FACES = "faces";
        String JSP = "jsp";
    }

    /**
     * Type of the navigation which should be used by the {@link javax.faces.application.NavigationHandler}
     */
    enum NavigationMode
    {
        DEFAULT, FORWARD, REDIRECT
    }

    /**
     * Mode specifies if JSF2 should include view-params
     */
    enum ViewParameterMode
    {
        DEFAULT, INCLUDE, EXCLUDE
    }

    static class ViewConfigPreProcessor implements ConfigPreProcessor<View>
    {
        @Override
        public View beforeAddToConfig(View view, ViewConfigNode viewConfigNode)
        {
            validateViewMetaData(view, viewConfigNode);

            boolean defaultValueReplaced = false;

            View.NavigationMode navigation = view.navigation();
            View.ViewParameterMode viewParams = view.viewParams();

            /*
             * base path
             */
            NameBuilder basePathBuilder = getBasePathBuilder(view);
            String basePath = basePathBuilder.build(view, viewConfigNode);

            if (basePathBuilder.isDefaultValueReplaced())
            {
                defaultValueReplaced = true;
            }

            /*
             * file name
             */
            NameBuilder fileNameBuilder = getFileNameBuilder(view);
            String name = fileNameBuilder.build(view, viewConfigNode);

            if (fileNameBuilder.isDefaultValueReplaced())
            {
                defaultValueReplaced = true;
            }

            /*
             * extension
             */
            NameBuilder extensionBuilder = getExtensionBuilder(view);
            String extension = extensionBuilder.build(view, viewConfigNode);

            if (extensionBuilder.isDefaultValueReplaced())
            {
                defaultValueReplaced = true;
            }

            /*
             * navigation
             */
            if (View.NavigationMode.DEFAULT.equals(navigation) || navigation == null)
            {
                defaultValueReplaced = true;
                navigation = FORWARD;
            }

            if (View.ViewParameterMode.DEFAULT.equals(viewParams) || viewParams == null)
            {
                defaultValueReplaced = true;
                viewParams = EXCLUDE;
            }

            if (defaultValueReplaced)
            {
                View result = new ViewLiteral(basePath, name, extension, navigation, viewParams,
                        view.basePathBuilder(), view.fileNameBuilder(), view.extensionBuilder());
                updateNodeMetaData(viewConfigNode, result);
                return result;
            }
            return view;
        }

        protected void validateViewMetaData(View view, ViewConfigNode viewConfigNode)
        {
            String basePath = view.basePath();
            if (viewConfigNode.getSource().isInterface() && !"".equals(basePath) && basePath != null)
            {
                throw new IllegalStateException("Using @" + View.class.getName() + "#basePath isn't allowed on" +
                    " folder-nodes (= interfaces). Use @" + Folder.class.getName() + " for intended folder-nodes" +
                    " or a class instead of the interface for page-nodes.");
            }
        }

        private void updateNodeMetaData(ViewConfigNode viewConfigNode, View view)
        {
            Set<Annotation> metaData = viewConfigNode.getMetaData();

            Iterator<? extends Annotation> metaDataIterator = metaData.iterator();

            while (metaDataIterator.hasNext())
            {
                if (View.class.equals(metaDataIterator.next().annotationType()))
                {
                    metaDataIterator.remove();
                    break;
                }
            }
            metaData.add(view);
        }

        private NameBuilder getBasePathBuilder(View view)
        {
            NameBuilder basePathBuilder;
            if (DefaultBasePathBuilder.class.equals(view.basePathBuilder()))
            {
                String customDefaultBasePathBuilderClassName =
                    JsfBaseConfig.ViewConfigCustomization.CUSTOM_DEFAULT_BASE_PATH_BUILDER;

                if (customDefaultBasePathBuilderClassName != null)
                {
                    basePathBuilder =
                        (NameBuilder)ClassUtils.tryToInstantiateClassForName(customDefaultBasePathBuilderClassName);
                }
                else
                {
                    basePathBuilder = new DefaultBasePathBuilder();
                }
            }
            else
            {
                basePathBuilder = ClassUtils.tryToInstantiateClass(view.basePathBuilder());
            }
            return basePathBuilder;
        }

        private NameBuilder getFileNameBuilder(View view)
        {
            NameBuilder fileNameBuilder;
            if (DefaultFileNameBuilder.class.equals(view.fileNameBuilder()))
            {
                String customDefaultFileNameBuilderClassName =
                    JsfBaseConfig.ViewConfigCustomization.CUSTOM_DEFAULT_FILE_NAME_BUILDER;

                if (customDefaultFileNameBuilderClassName != null)
                {
                    fileNameBuilder =
                        (NameBuilder)ClassUtils.tryToInstantiateClassForName(customDefaultFileNameBuilderClassName);
                }
                else
                {
                    fileNameBuilder = new DefaultFileNameBuilder();
                }
            }
            else
            {
                fileNameBuilder = ClassUtils.tryToInstantiateClass(view.fileNameBuilder());
            }
            return fileNameBuilder;
        }

        private NameBuilder getExtensionBuilder(View view)
        {
            NameBuilder extensionBuilder;
            if (DefaultExtensionBuilder.class.equals(view.extensionBuilder()))
            {
                String customDefaultExtensionBuilderClassName =
                    JsfBaseConfig.ViewConfigCustomization.CUSTOM_DEFAULT_EXTENSION_BUILDER;

                if (customDefaultExtensionBuilderClassName != null)
                {
                    extensionBuilder =
                        (NameBuilder)ClassUtils.tryToInstantiateClassForName(customDefaultExtensionBuilderClassName);
                }
                else
                {
                    extensionBuilder = new DefaultExtensionBuilder();
                }
            }
            else
            {
                extensionBuilder = ClassUtils.tryToInstantiateClass(view.extensionBuilder());
            }
            return extensionBuilder;
        }

        //it's possible that the given source is a folder-node
        //e.g. @View(navigation = REDIRECT) specified for a whole folder
        private static boolean isView(Class source)
        {
            return !Modifier.isAbstract(source.getModifiers()) && !Modifier.isInterface(source.getModifiers());
        }
    }

    //TODO discuss if we use a central interface in the spi package
    //advantage: can be reused
    //disadvantage: a wrong builder can get assigned more easily, show usages will list more
    interface NameBuilder
    {
        String build(View view, ViewConfigNode viewConfigNode);

        boolean isDefaultValueReplaced();
    }

    abstract class AbstractNameBuilder implements NameBuilder
    {
        protected boolean defaultValueReplaced = false;

        public boolean isDefaultValueReplaced()
        {
            return defaultValueReplaced;
        }
    }

    class DefaultBasePathBuilder extends AbstractNameBuilder
    {
        @Override
        public String build(View view, ViewConfigNode viewConfigNode)
        {
            String basePath = view.basePath();
            Class source = viewConfigNode.getSource();

            if (("".equals(basePath) || basePath == null) &&
                    ViewConfigPreProcessor.isView(source) /*only calc the path for real pages*/)
            {
                this.defaultValueReplaced = true;

                basePath = NamingConventionUtils.toPath(viewConfigNode.getParent());
            }

            if (basePath != null && basePath.startsWith("."))
            {
                basePath = NamingConventionUtils.toPath(viewConfigNode.getParent()) + basePath.substring(1);

                this.defaultValueReplaced = true;
            }

            if (basePath != null && !basePath.startsWith(".") && !basePath.startsWith("/"))
            {
                basePath = NamingConventionUtils.toPath(viewConfigNode.getParent()) + basePath;

                this.defaultValueReplaced = true;
            }

            if (basePath != null && !basePath.endsWith("/"))
            {
                basePath = basePath + "/";

                this.defaultValueReplaced = true;
            }

            if (basePath != null && basePath.contains("//"))
            {
                basePath = basePath.replace("//", "/");

                this.defaultValueReplaced = true;
            }

            return basePath;
        }
    }

    class DefaultFileNameBuilder extends AbstractNameBuilder
    {
        @Override
        public String build(View view, ViewConfigNode viewConfigNode)
        {
            String name = view.name();
            Class source = viewConfigNode.getSource();

            if (("".equals(name) || name == null) &&
                    ViewConfigPreProcessor.isView(source) /*only calc the path for real pages*/)
            {
                this.defaultValueReplaced = true;
                String className = viewConfigNode.getSource().getSimpleName();
                name = className.substring(0, 1).toLowerCase() + className.substring(1);
            }

            return name;
        }
    }

    class DefaultExtensionBuilder extends AbstractNameBuilder
    {
        @Override
        public String build(View view, ViewConfigNode viewConfigNode)
        {
            String extension = view.extension();

            if (View.Extension.DEFAULT.equals(extension) || extension == null)
            {
                defaultValueReplaced = true;
                extension = XHTML;
            }

            return extension;
        }
    }
}
