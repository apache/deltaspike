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
