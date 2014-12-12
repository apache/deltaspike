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

DeltaSpike site
---------------

DeltaSpike site uses [Asciidoc](http://www.methods.co.nz/asciidoc/). You're welcome to contribute.

License
-------
Apache DeltaSpike is licensed under ALv2.
See the LICENSE file for the full license text.

Publish procedure
-----------------

To publish the site content at [DeltaSpike Site](http://deltaspike.apache.org/) you have do the following steps:

Put the following information in your ~/.m2/settings.xml file

    <server>
      <id>deltaspike-site</id>
      <username><YOUR_USERNAME></username>
      <password><YOUR_PASSWORD></password>
    </server>

To publish to [staging area](http://deltaspike.apache.org/staging/), run:

    mvn clean site-deploy -Pstaging

To publish to [production area](http://deltaspike.apache.org/), run:

    mvn clean site-deploy

After log in to <https://cms.apache.org/deltaspike/publish> and click on the `Submit` button.
