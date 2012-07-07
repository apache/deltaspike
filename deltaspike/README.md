title: Apache DeltaSpike

Notice:    Licensed to the Apache Software Foundation (ASF) under one
           or more contributor license agreements.  See the NOTICE file
           distributed with this work for additional information
           regarding copyright ownership.  The ASF licenses this file
           to you under the Apache License, Version 2.0 (the
           "License"); you may not use this file except in compliance
           with the License.  You may obtain a copy of the License at
           .
             http://www.apache.org/licenses/LICENSE-2.0
           .
           Unless required by applicable law or agreed to in writing,
           software distributed under the License is distributed on an
           "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
           KIND, either express or implied.  See the License for the
           specific language governing permissions and limitations
           under the License.


About Apache DeltaSpike
-----------------------
Apache DeltaSpike is a CDI Extensions Project which contains
the best mature features of JBoss Seam3, Apache MyFaces CODI
and other CDI Extensions projects.


License
-------
Apache DeltaSpike is licensed under ALv2.
See the LICENSE file for the full license text.


Building Apache DeltaSpike
--------------------
DeltaSpike is container agnostic. It just needs a working CDI container
integration via Arquillian [1]. The different containers get activated
via Maven Profiles. The following profiles exist so far:

 * OWB
 * Weld

For building DeltaSpike with JBoss Weld [2] (Reference Implementation)
invoke the following commandline:

$> mvn clean install -PWeld

For building DeltaSpike with Apache OpenWebBeans [3] execute the
following command

$> mvn clean install -POWB



[1] http://www.jboss.org/arquillian
[2] http://docs.jboss.org/weld/reference/1.1.0.Final/en-US/html/
[3] http://openwebbeans.apache.org
