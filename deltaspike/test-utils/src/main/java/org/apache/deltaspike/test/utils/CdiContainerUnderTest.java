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
package org.apache.deltaspike.test.utils;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

/**
 * A small helper class which checks if the container which is currently being tested matches the given version RegExp
 */
public class CdiContainerUnderTest
{
    private CdiContainerUnderTest()
    {
        // utility class ct
    }

    /**
     * Checks whether the current container matches the given version regexps.
     * 
     * @param containerRegExps
     *            container versions to test against. e.g. 'owb-1\\.0\\..*' or 'weld-2\\.0\\.0\\..*'
     */
    public static boolean is(String... containerRegExps)
    {
        String containerVersion = System.getProperty("cdicontainer.version");

        if (containerVersion == null)
        {
            return false;
        }

        for (String containerRe : containerRegExps)
        {
            if (containerVersion.matches(containerRe))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Verify if the runtime is using the following CdiImplementation
     * 
     * @param cdiImplementation
     * @param versionRange
     *            optional - If not defined it will used the range defined on {@link CdiImplementation}
     * @return
     * @throws InvalidVersionSpecificationException
     */
    public static boolean isCdiVersion(CdiImplementation cdiImplementation, String versionRange)
        throws InvalidVersionSpecificationException
    {

        Class implementationClass = tryToLoadClassForName(cdiImplementation.getImplementationClassName());

        if (implementationClass == null)
        {
            return false;
        }

        VersionRange range = VersionRange.createFromVersionSpec(versionRange == null ? cdiImplementation
                .getVersionRange() : versionRange);
        String containerVersion = getJarSpecification(implementationClass);
        return containerVersion != null && range.containsVersion(new DefaultArtifactVersion(containerVersion));
    }

    private static Class tryToLoadClassForName(String name)
    {
        try
        {
            return loadClassForName(name);
        }
        catch (ClassNotFoundException e)
        {
            // do nothing - it's just a try
            return null;
        }
    }

    private static Class loadClassForName(String name) throws ClassNotFoundException
    {
        try
        {
            // Try WebApp ClassLoader first
            return Class.forName(name, false, // do not initialize for faster startup
                    getClassLoader(null));
        }
        catch (ClassNotFoundException ignore)
        {
            // fallback: Try ClassLoader for ClassUtils (i.e. the myfaces.jar lib)
            return Class.forName(name, false, // do not initialize for faster startup
                    CdiContainerUnderTest.class.getClassLoader());
        }
    }

    private static ClassLoader getClassLoader(Object o)
    {
        if (System.getSecurityManager() != null)
        {
            return AccessController.doPrivileged(new GetClassLoaderAction(o));
        }
        else
        {
            return getClassLoaderInternal(o);
        }
    }

    private static ClassLoader getClassLoaderInternal(Object o)
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        if (loader == null && o != null)
        {
            loader = o.getClass().getClassLoader();
        }

        if (loader == null)
        {
            loader = CdiContainerUnderTest.class.getClassLoader();
        }

        return loader;
    }

    private static String getJarVersion(Class targetClass)
    {
        String manifestFileLocation = getManifestFileLocationOfClass(targetClass);

        try
        {
            return new Manifest(new URL(manifestFileLocation).openStream())
                    .getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static String getJarSpecification(Class targetClass)
    {
        String manifestFileLocation = getManifestFileLocationOfClass(targetClass);

        try
        {
            return new Manifest(new URL(manifestFileLocation).openStream())
                    .getMainAttributes().getValue(Attributes.Name.SPECIFICATION_VERSION);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static String getManifestFileLocationOfClass(Class targetClass)
    {
        String manifestFileLocation;

        try
        {
            manifestFileLocation = getManifestLocation(targetClass);
        }
        catch (Exception e)
        {
            // in this case we have a proxy
            manifestFileLocation = getManifestLocation(targetClass.getSuperclass());
        }
        return manifestFileLocation;
    }

    private static String getManifestLocation(Class targetClass)
    {
        String classFilePath = targetClass.getCanonicalName().replace('.', '/') + ".class";
        String manifestFilePath = "/META-INF/MANIFEST.MF";

        String classLocation = targetClass.getResource(targetClass.getSimpleName() + ".class").toString();
        return classLocation.substring(0, classLocation.indexOf(classFilePath) - 1) + manifestFilePath;
    }

    private static class GetClassLoaderAction implements PrivilegedAction<ClassLoader>
    {
        private Object object;

        GetClassLoaderAction(Object object)
        {
            this.object = object;
        }

        @Override
        public ClassLoader run()
        {
            try
            {
                return getClassLoaderInternal(object);
            }
            catch (Exception e)
            {
                return null;
            }
        }
    }

    public static boolean isNotTomEE()
    {
        return !System.getProperty("cdicontainer.version").startsWith("tomee");
    }
}
