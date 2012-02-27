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
package org.apache.deltaspike.test.util;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * Lots of neat little helpers to more easily create JavaArchives from marker files on the classpath.
 * This should finally get moved to ShrinkWrap core!
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @deprecated This class should get moved to ShrinkWrap itself!
 */
public class ShrinkWrapArchiveUtil
{
    private static final Logger LOG = Logger.getLogger(ShrinkWrapArchiveUtil.class.getName());
    /**
     * Resolve all markerFiles from the current ClassPath and package the root nodes
     * into a JavaArchive.
     * @param classLoader to use
     * @param markerFile
     * @param includeIfPackageExists if not null, we will only create JavaArchives if the given package exists
     * @param excludeIfPackageExists if not null, we will <b>not</b> create JavaArchives if the given package exists.
     *                               This has a higher precedence than includeIfPackageExists.
     * @return
     */
    public static JavaArchive[] getArchives(ClassLoader classLoader,
                                            String markerFile,
                                            String[] includeIfPackageExists,
                                            String[] excludeIfPackageExists)
    {
        if (classLoader == null) {
            classLoader = ShrinkWrapArchiveUtil.class.getClassLoader();
        }

        try {
            Enumeration<URL> foundFiles = classLoader.getResources(markerFile);

            List<JavaArchive> archives = new ArrayList<JavaArchive>();

            while (foundFiles.hasMoreElements()) {
                URL foundFile = foundFiles.nextElement();
                LOG.fine("Evaluating Java ClassPath URL " + foundFile.toExternalForm());

                JavaArchive archive = createArchive(foundFile, markerFile, includeIfPackageExists, excludeIfPackageExists);
                if (archive != null) {
                    LOG.info("Adding Java ClassPath URL as JavaArchive " + foundFile.toExternalForm());
                    archives.add(archive);
                }
            }

            return archives.toArray(new JavaArchive[archives.size()]);
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }

    private static JavaArchive createArchive(URL foundFile, String markerFile,
                                             String[] includeIfPackageExists, String[] excludeIfPackageExists)
            throws IOException {
        String urlString = foundFile.toString();
        int idx = urlString.lastIndexOf(markerFile);
        urlString = urlString.substring(0, idx);

        String jarUrlPath = isJarUrl(urlString);
        if (jarUrlPath != null)
        {
            return addJarArchive((new URL(ensureCorrectUrlFormat(jarUrlPath))).openStream(),
                    includeIfPackageExists, excludeIfPackageExists);
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

            return addFileArchive(f, includeIfPackageExists, excludeIfPackageExists);
        }
    }

    private static JavaArchive addJarArchive(InputStream inputStream,
                                             String[] includeIfPackageExists,
                                             String[] excludeIfPackageExists)
            throws IOException {
        JavaArchive ret = null;
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class);

        if (includeIfPackageExists == null) {
            // no include rule, thus add it immediately
            ret = javaArchive;
        }

        JarInputStream jar = new JarInputStream(inputStream);
        try {
            for (ZipEntry jarEntry = jar.getNextEntry(); jarEntry != null; jarEntry = jar.getNextEntry()) {
                String entryName = jarEntry.getName();

                if (jarEntry.isDirectory()) {
                    // exclude rule
                    if (excludeIfPackageExists(entryName, excludeIfPackageExists)) {
                        return null;
                    }

                    if (ret == null && includeIfPackageExists(entryName, includeIfPackageExists)) {
                        ret = javaArchive;
                    }

                    continue;
                }

                if (entryName.endsWith(".class")) {
                    String className = pathToClassName(entryName.substring(0, entryName.length()-(".class".length())));
                    javaArchive.addClass(className);
                }
                else {
                    javaArchive.addAsResource(entryName);
                }
            }
        }
        finally {
            try {
                jar.close();
            }
            catch (IOException ignored) {
                // all fine
            }
        }

        return ret;
    }

    private static JavaArchive addFileArchive(File archiveBasePath,
                                              String[] includeIfPackageExists,
                                              String[] excludeIfPackageExists)
            throws IOException {
        if (!archiveBasePath.exists()) {
            return null;
        }

        JavaArchive ret = null;
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class);

        if (includeIfPackageExists == null) {
            // no include rule, thus add it immediately
            ret = javaArchive;
        }

        int basePathLength = archiveBasePath.getAbsolutePath().length() + 1;

        for (File archiveEntry : collectArchiveEntries(archiveBasePath) ) {
            String entryName = archiveEntry.getAbsolutePath().substring(basePathLength);

            // exclude rule
            if (excludeIfPackageExists(entryName, excludeIfPackageExists)) {
                continue;
            }

            // include rule
            if (ret == null && includeIfPackageExists(entryName, includeIfPackageExists)) {
                ret = javaArchive;
            }

            if (entryName.endsWith(".class")) {
                String className = pathToClassName(entryName.substring(0, entryName.length()-(".class".length())));

                javaArchive.addClass(className);
            }
            else {
                javaArchive.addAsResource(entryName.replace('\\', '/'));
            }
        }

        return ret;
    }

    private static List<File> collectArchiveEntries(File archiveBasePath)
    {
        if (archiveBasePath.isDirectory()) {
            List<File> archiveEntries = new ArrayList<File>();
            File[] files = archiveBasePath.listFiles();

            for (File file : files) {
                if (file.isDirectory()) {
                    archiveEntries.addAll(collectArchiveEntries(file));
                }
                else {
                    archiveEntries.add(file);
                }
            }

            return archiveEntries;
        }

        return Collections.EMPTY_LIST;
    }


    private static boolean excludeIfPackageExists(String jarEntryName, String[] excludeOnPackages) {
        if (excludeOnPackages != null) {
            String packageName = pathToClassName(jarEntryName);

            for (String excludeOnPackage : excludeOnPackages) {
                if (packageName.startsWith(excludeOnPackage)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean includeIfPackageExists(String jarEntryName, String[] includeOnPackages) {
        if (includeOnPackages == null ) {
            return true;
        }

        String packageName = pathToClassName(jarEntryName);

        for (String includeOnPackage : includeOnPackages) {
            if (packageName.startsWith(includeOnPackage)) {
                return true;
            }
        }

        return false;
    }

    /**
     * check if the given url path is a Jar
     * @param urlPath
     * @return
     */
    private static String isJarUrl(String urlPath) {
        // common prefixes of the url are: jar: (tomcat), zip: (weblogic) and wsjar: (websphere)
        final int jarColon = urlPath.indexOf(':');
        if (urlPath.endsWith("!/") && jarColon > 0) {
            urlPath = urlPath.substring(jarColon + 1, urlPath.length() - 2);
            return urlPath;
        }

        return null;
    }

    private static String ensureCorrectUrlFormat(String url) {
        //fix for wls
        if(!url.startsWith("file:/")) {
            url = "file:/" + url;
        }
        return url;
    }

    private static String pathToClassName(String pathName) {
        return pathName.replace('/', '.').replace('\\', '.');   // replace unix and windows separators
    }


}
