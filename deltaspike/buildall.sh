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
mvn clean install -POWB -Dowb.version=1.1.8 | tee mvn-owb1_1_8.log
mvn clean install -POWB -Dowb.version=1.2.6 -Dopenejb.owb.version=1.1.8 | tee mvn-owb1_2_6.log
mvn clean install -POWB -Dowb.version=1.2.7-SNAPSHOT -Dopenejb.owb.version=1.1.8 | tee mvn-owb1_2_7.log
mvn clean install -POWB2 -Dowb.version=2.0.0-SNAPSHOT -Dopenejb.owb.version=1.1.8 | tee mvn-owb2.0.0.log
mvn clean install -PWeld -Dweld.version=1.1.10.Final | tee mvn-weld1_1_10.log
mvn clean install  -Ptomee-build-managed -Dtomee.version=1.6.0 -Dopenejb.version=4.6.0 -Dopenejb.owb.version=1.2.1 | tee mvn-tomee_1_6_0.log
mvn clean install  -Ptomee-build-managed -Dtomee.version=1.5.2 -Dopenejb.version=4.5.2 -Dopenejb.owb.version=1.1.8 | tee mvn-tomee_1_5_2.log
mvn clean install  -Ptomee-build-managed -Dtomee.version=1.7.0 -Dopenejb.version=4.7.0 -Dopenejb.owb.version=1.2.6 | tee mvn-tomee_1_7_0.log
mvn clean install -Pjbossas-build-managed-7 | tee mvn-jbossas_7.log


# and now for the result check
tail mvn-*.log | less
