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

import org.apache.deltaspike.core.api.config.view.metadata.annotation.ViewMetaData;
import org.apache.deltaspike.core.spi.config.view.ConfigPreProcessor;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.api.literal.FolderLiteral;
import org.apache.deltaspike.jsf.util.NamingConventionUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
    String name() default "";

    static class FolderConfigPreProcessor implements ConfigPreProcessor<Folder>
    {
        @Override
        public Folder beforeAddToConfig(Folder folder, ViewConfigNode viewConfigNode)
        {
            boolean defaultValueReplaced = false;

            String name = folder.name();

            if (name == null) //null used as marker value for dynamically added instances
            {
                defaultValueReplaced = true;
                name = NamingConventionUtils.toPath(viewConfigNode);
            }

            //TODO if name.startsWith("./")

            if (defaultValueReplaced)
            {
                return new FolderLiteral(name);
            }
            return folder;
        }
    }
}
