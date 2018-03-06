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
package org.apache.deltaspike.core.util;

import javax.enterprise.inject.spi.Extension;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.deltaspike.core.api.config.base.CoreBaseConfig;

/**
 * Support for Containers with 'hierarchic BeanManagers'
 * This is mostly useful for EAR applications.
 *
 * Some EE Container scan the common shared EAR lib path and reuse this information
 * for the webapps in the EAR. This is actually the only approach a container can
 * do to prevent mem leaks and side effects spreading to different webapps.
 * Of course this also means that the webapps get their own (different)
 * instances of an Extension.
 *
 * To acknowledge this solution we provide a mechanism to lookup 'parent Extensions'
 * which is very similar to handling parent ClassLoaders.
 *
 * Please note that you need to enable this handling if you are running DeltaSpike
 * in an EAR on a container which supports parent Extensions.
 * You can do that by settting {@code "deltaspike.parent.extension.enabled"} to &quote;true&quote;
 *
 * All your Extension has to do is to register itself in
 * {@link javax.enterprise.inject.spi.BeforeBeanDiscovery}.
 * Later at boot time the Extension can lookup it's parent Extension instance and
 * e.g. check which classes got scanned in the parent ClassLoader.
 *
 * The ExtensionInfo automatically gets removed if the webapp gets undeployed.
 *
 * @see org.apache.deltaspike.core.api.config.base.CoreBaseConfig.ParentExtensionCustomization
 */
public final class ParentExtensionStorage
{

    private static Set<ExtensionStorageInfo> extensionStorage = new HashSet<ExtensionStorageInfo>();

    private ParentExtensionStorage()
    {
        // utility class ct
    }

    /**
     * Add info about an Extension to our storage
     * This method is usually called during boostrap via {@code &#064;Observes BeforeBeanDiscovery}.
     */
    public static synchronized void addExtension(Extension extension)
    {
        if (usingParentExtension())
        {
            removeAbandonedExtensions();

            ClassLoader classLoader = ClassUtils.getClassLoader(null);
            extensionStorage.add(new ExtensionStorageInfo(classLoader, extension));
        }
    }

    /**
     * When adding a new Extension we also clean up ExtensionInfos
     * from ClassLoaders which got unloaded.
     */

    private static boolean usingParentExtension()
    {
        return CoreBaseConfig.ParentExtensionCustomization.PARENT_EXTENSION_ENABLED;
    }

    private static void removeAbandonedExtensions()
    {
        Iterator<ExtensionStorageInfo> it = extensionStorage.iterator();
        while (it.hasNext())
        {
            ExtensionStorageInfo info = it.next();
            if (info.isAbandoned())
            {
                it.remove();
            }
        }
    }

    /**
     * @return the Extension from the same type but registered in a hierarchic 'parent' BeanManager
     */
    public static synchronized <T extends Extension> T getParentExtension(Extension extension)
    {
        if (usingParentExtension())
        {
            ClassLoader parentClassLoader = ClassUtils.getClassLoader(null).getParent();

            Iterator<ExtensionStorageInfo> extIt = extensionStorage.iterator();
            while (extIt.hasNext())
            {
                ExtensionStorageInfo extensionInfo = extIt.next();
                if (!extensionInfo.isAbandoned() && // weak reference case
                    extension.getClass().equals(extensionInfo.getExtension().getClass()) &&
                    extensionInfo.getClassLoader().equals(parentClassLoader))
                {
                    return (T) extensionInfo.getExtension();
                }
            }
        }
        return null;
    }


    /**
     * Information about an Extension instance and in which classloader it got used
     */
    private static class ExtensionStorageInfo
    {
        // we use WeakReferences to allow perfect unloading of any webapp ClassLoader
        private final WeakReference<ClassLoader> classLoader;
        private final WeakReference<Extension> extension;

        ExtensionStorageInfo(ClassLoader classLoader, Extension extension)
        {
            this.classLoader = new WeakReference<ClassLoader>(classLoader);
            this.extension = new WeakReference<Extension>(extension);
        }

        boolean isAbandoned()
        {
            return classLoader.get() == null || extension.get() == null;
        }

        ClassLoader getClassLoader()
        {
            return classLoader.get();
        }

        Extension getExtension()
        {
            return extension.get();
        }
    }
}
