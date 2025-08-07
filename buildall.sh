#!/bin/sh
#####################################################################################
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#####################################################################################
#
# this is a small helper script for building a few container constellations locally
# you can easily check the output via $> tail mvn-*.log | less
#
#####################################################################################

rm mvn-*log


# CDI-2.0, EE8

# works fine with Java11
mvn clean install -POWB | tee mvn-owb4.0.3.log
mvn clean install -Ptomee-build-managed -Dtomee.version=10.0.0-M1 | tee mvn-tomee10.0.0-M1.log

mvn clean install -PWeld -Dweld.version=5.1.2.Final | tee mvn-weld5.1.2.log

# requires Java 17
mvn clean install -Ptomee-build-managed -Dtomee.version=10.1.0 | tee mvn-tomee10.0.1.log


# and now for the result check
tail mvn-*.log | less
