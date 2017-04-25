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


import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;


/**
 * Lots of neat little helpers to more easily create JavaArchives from marker files on the classpath.
 * This should finally get moved to ShrinkWrap core!
 *
 * TODO This class should get moved to ShrinkWrap itself!
 */
public class ShrinkWrapArchiveUtil
{
    private static final Logger LOG = Logger.getLogger(ShrinkWrapArchiveUtil.class.getName());

    private ShrinkWrapArchiveUtil()
    {
        // private ct for utility class
    }

    /**
     * Resolve all markerFiles from the current ClassPath and package the root nodes
     * into a JavaArchive.
     *
     * @param classLoader            to use
     * @param markerFile             finding this marker file will trigger creating the JavaArchive.
     * @param includeIfPackageExists if not null, we will only create JavaArchives if the given package exists
     * @param excludeIfPackageExists if not null, we will <b>not</b> create JavaArchives if the given package exists.
     *                               This has a higher precedence than includeIfPackageExists.
     */
    public static JavaArchive[] getArchives(ClassLoader classLoader,
                                            String markerFile,
                                            String[] includeIfPackageExists,
                                            String[] excludeIfPackageExists,
                                            String archiveName)
    {
        if (classLoader == null)
        {
            classLoader = ShrinkWrapArchiveUtil.class.getClassLoader();
        }

        try
        {
            Enumeration<URL> foundFiles = classLoader.getResources(markerFile);

            List<JavaArchive> archives = new ArrayList<JavaArchive>();
            int numArchives = 0;

            while (foundFiles.hasMoreElements())
            {
                URL foundFile = foundFiles.nextElement();
                LOG.fine("Evaluating Java ClassPath URL " + foundFile.toExternalForm());
                String suffix = (numArchives == 0) ? "" : Integer.toString(numArchives);

                JavaArchive archive
                    = createArchive(foundFile, markerFile, includeIfPackageExists, excludeIfPackageExists, 
                        archiveName + suffix);
                if (archive != null)
                {
                    LOG.info("Test " + getTestName()
                            + " Adding Java ClassPath URL as JavaArchive " + foundFile.toExternalForm());
                    archives.add(archive);
                    numArchives++;
                }
            }

            return archives.toArray(new JavaArchive[archives.size()]);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }

    }

    private static JavaArchive createArchive(URL foundFile, String markerFile,
                                             String[] includeIfPackageExists,
                                             String[] excludeIfPackageExists,
                                             String archiveName)
        throws IOException
    {
        String urlString = foundFile.toString();
        int idx = urlString.lastIndexOf(markerFile);
        urlString = urlString.substring(0, idx);

        String jarUrlPath = isJarUrl(urlString);
        if (jarUrlPath != null)
        {
            JavaArchive foundJar = ShrinkWrap.createFromZipFile(JavaArchive.class, new File(URI.create(jarUrlPath)));

            if (excludeIfPackageExists != null)
            {
                for (String excludePackage : excludeIfPackageExists)
                {
                    if (foundJar.contains(excludePackage.replaceAll("\\.", "\\/")))
                    {
                        return null;
                    }
                }
            }
            if (includeIfPackageExists != null)
            {
                for (String includePackage : includeIfPackageExists)
                {
                    if (foundJar.contains(includePackage.replaceAll("\\.", "\\/")))
                    {
                        return foundJar;
                    }
                }
            }
            return null; // couldn't find any jar
        }
        else
        {
            File f = new File( (new URL(ensureCorrectUrlFormat(urlString))).getFile() );
            if (!f.exists())
            {
                // try a fallback if the URL contains %20 -> spaces
                if (urlString.contains("%20"))
                {
                    urlString = urlString.replaceAll("%20", " ");
                    f = new File( (new URL(ensureCorrectUrlFormat(urlString))).getFile() );
                }

            }

            return addFileArchive(f, includeIfPackageExists, excludeIfPackageExists, archiveName);
        }
    }

    private static JavaArchive addFileArchive(File archiveBasePath,
                                              String[] includeIfPackageExists,
                                              String[] excludeIfPackageExists,
                                              String archiveName)
        throws IOException
    {
        if (!archiveBasePath.exists())
        {
            return null;
        }

        if (archiveName == null)
        {
            archiveName = UUID.randomUUID().toString();
        }

        JavaArchive ret = null;
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar");

        if (includeIfPackageExists == null)
        {
            // no include rule, thus add it immediately
            ret = javaArchive;
        }

        int basePathLength = archiveBasePath.getAbsolutePath().length() + 1;

        for (File archiveEntry : collectArchiveEntries(archiveBasePath) )
        {
            String entryName = archiveEntry.getAbsolutePath().substring(basePathLength);

            // exclude rule
            if (excludeIfPackageExists(entryName, excludeIfPackageExists))
            {
                continue;
            }

            // include rule
            if (ret == null && includeIfPackageExists(entryName, includeIfPackageExists))
            {
                ret = javaArchive;
            }

            if (entryName.endsWith(".class"))
            {
                String className
                    = pathToClassName(entryName.substring(0, entryName.length() - (".class".length())));

                try
                {
                    javaArchive.addClass(className);
                }
                catch (Throwable t)
                {
                    LOG.info("Ignoring class " + className + " due to " + t.getMessage());
                }
            }
            else
            {
                javaArchive.addAsResource(archiveEntry, entryName.replace('\\', '/'));
            }
        }

        return ret;
    }

    private static List<File> collectArchiveEntries(File archiveBasePath)
    {
        if (archiveBasePath.isDirectory())
        {
            List<File> archiveEntries = new ArrayList<File>();
            File[] files = archiveBasePath.listFiles();

            for (File file : files)
            {
                if (file.isDirectory())
                {
                    archiveEntries.addAll(collectArchiveEntries(file));
                }
                else
                {
                    archiveEntries.add(file);
                }
            }

            return archiveEntries;
        }

        return Collections.emptyList();
    }


    private static boolean excludeIfPackageExists(String jarEntryName, String[] excludeOnPackages)
    {
        if (excludeOnPackages != null)
        {
            String packageName = pathToClassName(jarEntryName);

            for (String excludeOnPackage : excludeOnPackages)
            {
                if (packageName.startsWith(excludeOnPackage))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean includeIfPackageExists(String jarEntryName, String[] includeOnPackages)
    {
        if (includeOnPackages == null )
        {
            return true;
        }

        String packageName = pathToClassName(jarEntryName);

        for (String includeOnPackage : includeOnPackages)
        {
            if (packageName.startsWith(includeOnPackage))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * check if the given url path is a Jar
     * @param urlPath to check
     */
    private static String isJarUrl(String urlPath)
    {
        // common prefixes of the url are: jar: (tomcat), zip: (weblogic) and wsjar: (websphere)
        final int jarColon = urlPath.indexOf(':');
        if (urlPath.endsWith("!/") && jarColon > 0)
        {
            urlPath = urlPath.substring(jarColon + 1, urlPath.length() - 2);
            return urlPath;
        }

        return null;
    }

    private static String ensureCorrectUrlFormat(String url)
    {
        //fix for wls
        if (!url.startsWith("file:/"))
        {
            url = "file:/" + url;
        }
        return url;
    }

    private static String pathToClassName(String pathName)
    {
        return pathName.replace('/', '.').replace('\\', '.');   // replace unix and windows separators
    }


    public static String getTestName()
    {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String testName = "unknown";
        for (StackTraceElement ste : stackTraceElements)
        {
            if (ste.getClassName().contains("Test"))
            {
                testName = ste.getClassName();
                break;
            }
        }

        return testName;
    }
}
