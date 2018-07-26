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

# CDI-1.0, EE6
mvn clean install -POWB -Dowb.version=1.2.7 -Dopenejb.owb.version=1.2.7 | tee mvn-owb1_2_7.log
mvn clean install -PWeld1 -Dweld.version=1.1.10.Final | tee mvn-weld1_1_10.log
mvn clean install -Ptomee-build-managed -Dtomee.version=1.7.5 -Dopenejb.version=4.7.5 -Dopenejb.owb.version=1.2.8 | tee mvn-tomee_1_7_5.log

# jbossas7 is broken on Java8, it strictly requires Java7
# mvn clean install -Pjbossas-build-managed-7 | tee mvn-jbossas_7.log

# CDI-1.2, EE7
mvn clean install -POWB15,OpenEJB-TomEE -Dowb.version=1.7.5 -Dopenejb.owb.version=1.7.5 -Dopenejb.version=7.0.5 | tee mvn-owb1.7.5.log
mvn clean install -PWeld2 -Dweld.version=2.4.6.Final | tee mvn-weld2_4_6.log
mvn clean install -Pwildfly-build-managed | tee mvn-wildfly9.log
mvn clean install -Ptomee7-build-managed,OpenEJB-TomEE -Dtomee.version=7.0.4 -Dopenejb.version=7.0.4 -Dopenejb.owb.version=1.7.4 | tee mvn-tomee_7_0_4.log

# CDI-2.0, EE8

mvn clean install -POWB2 | tee mvn-owb2_0_5.log
mvn clean install -POWB2 | tee mvn-owb2_0_5.log


# and now for the result check
tail mvn-*.log | less
