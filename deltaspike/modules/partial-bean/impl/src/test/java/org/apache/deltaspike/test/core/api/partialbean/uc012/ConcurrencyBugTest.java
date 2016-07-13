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

package org.apache.deltaspike.test.core.api.partialbean.uc012;

import org.apache.deltaspike.test.core.api.partialbean.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

@RunWith(Arquillian.class)
public class ConcurrencyBugTest
{

   @Deployment
   public static WebArchive war()
   {
      final String simpleName = ConcurrencyBugTest.class.getSimpleName();
      final String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

      final JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
            .addPackage(ConcurrencyBugTest.class.getPackage())
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

      final WebArchive webArchive =  ShrinkWrap.create(WebArchive.class, archiveName + ".war")
            .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndPartialBeanArchive())
            .addAsLibraries(testJar)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

      return webArchive;
   }

   @Inject
   private PartialBean bean;

   @Test
   public void testWithConcurrency() throws Exception
   {
      ThreadFactory threadFactory = Executors.defaultThreadFactory();
      ExecutorService executor = new ThreadPoolExecutor(5, 10, 60, SECONDS, new SynchronousQueue<Runnable>(),
            threadFactory, new BlockPolicy());
      int iterations = 100;
      List<Future<String>> results = new ArrayList<Future<String>>(iterations);
      for (int i = 0; i < iterations; i++) {
         results.add(executor.submit(new BeanCaller(bean)));
      }
      executor.shutdown();
      executor.awaitTermination(60, SECONDS);
      for (int i = 0; i < iterations; i++)
      {
         results.get(i).get();
      }
   }

   private class BeanCaller implements Callable<String>
   {
      private final PartialBean partialBean;

      private BeanCaller(PartialBean partialBean) {
         this.partialBean = partialBean;
      }

      @Override
      public String call() {
         try {
            return partialBean.getValue();
         }
         catch (NullPointerException e)
         {
            e.printStackTrace();
            throw e;
         }
      }
   }
}
