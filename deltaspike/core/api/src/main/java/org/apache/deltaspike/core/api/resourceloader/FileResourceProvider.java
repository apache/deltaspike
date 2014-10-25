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
package org.apache.deltaspike.core.api.resourceloader;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A file-based resource provider, looking for a file based on the name.
 */
@ApplicationScoped
public class FileResourceProvider extends AbstractResourceProvider
{
    private static final Logger logger = Logger.getLogger(FileResourceProvider.class.getName());
    InputStream readFile(final String name)
    {
        File f = new File(name);
        if (f.exists() && f.canRead() && f.isFile())
        {
            try
            {
                return new FileInputStream(f);
            }
            catch (FileNotFoundException e)
            {
                logger.log(Level.SEVERE, "Problem reading resource.", e);
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public InputStream readStream(InjectableResource injectableResource)
    {
        return readFile(injectableResource.location());
    }
}
