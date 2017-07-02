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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class PropertyFileUtilsTest
{
    @Parameterized.Parameters
    public static Iterable<Object[]> cases() throws MalformedURLException
    {
        return Arrays.<Object[]>asList(
                new Object[] // classpath
                        {
                            new Case("test.properties")
                            {
                                @Override
                                public void run()
                                {
                                    assertTrue(result.hasMoreElements());
                                    result.nextElement();
                                    assertFalse(result.hasMoreElements());
                                }
                            }
                        },
                new Object[] // file path
                        {
                                new Case("src/test")
                                {
                                    @Override
                                    public void run()
                                    {
                                        assertTrue(result.hasMoreElements());
                                        result.nextElement();
                                        assertFalse(result.hasMoreElements());
                                    }
                                }
                        },
                new Object[] // url
                        {
                                new Case(new File("src/test/resources/test.properties").toURI().toURL().toExternalForm())
                                {
                                    @Override
                                    public void run()
                                    {
                                        assertTrue(result.hasMoreElements());
                                        result.nextElement();
                                        assertFalse(result.hasMoreElements());
                                    }
                                }
                        },
                new Object[] // url not existent
                        {
                                new Case(new File("src/test/resources/test_not_existent.properties").toURI().toURL().toExternalForm())
                                {
                                    @Override
                                    public void run()
                                    {
                                        assertFalse(result.hasMoreElements());
                                    }
                                }
                        });
    }

    private final Case test;

    public PropertyFileUtilsTest(Case test)
    {
        this.test = test;
    }

    @Test
    public void run() throws IOException, URISyntaxException
    {
        test.result = PropertyFileUtils.resolvePropertyFiles(test.file);
        test.run();
    }

    private static abstract class Case implements Runnable
    {
        private String file;
        protected Enumeration<URL> result;

        private Case(final String file)
        {
            this.file = file;
        }
    }
}
