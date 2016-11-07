# Apache DeltaSpike

* [Documentation](https://deltaspike.apache.org/documentation/)
* [Mailing Lists](http://deltaspike.apache.org/community.html#Mailinglists)
* CDI 1.0 Build Status [![CDI 1.0 Build Status](https://builds.apache.org/buildStatus/icon?job=DeltaSpike for CDI 1.0)](https://builds.apache.org/view/A-D/view/DeltaSpike/job/DeltaSpike%20for%20CDI%201.0/)
* CDI 1.1/1.2 Build Status [![CDI 1.1/1.2 Build Status](https://builds.apache.org/buildStatus/icon?job=DeltaSpike for CDI 1.1 and 1.2)](https://builds.apache.org/view/A-D/view/DeltaSpike/job/DeltaSpike%20for%20CDI%201.1%20and%201.2/)
* [Contribution Guide](http://deltaspike.apache.org/source.html)
* [JIRA](https://issues.apache.org/jira/browse/DELTASPIKE)
* [Apache 2.0](https://git-wip-us.apache.org/repos/asf?p=deltaspike.git;a=blob;f=LICENSE.txt;hb=HEAD)


**Apache DeltaSpike** is a suite of portable CDI (Contexts & Dependency
Injection) extensions intended to make application development easier when
working with CDI and Java EE.  Some of its key features include:

- A core runtime that supports component configuration, type safe messaging
and internationalization, and exception handling.
- A suite of utilities to make programmatic bean lookup easier.
- A plugin for Java SE to bootstrap both JBoss Weld and Apache OpenWebBeans
outside of a container.
- JSF integration, including backporting of JSF 2.2 features for Java EE 6.
- JPA integration and transaction support.
- A Data module, to create an easy to use repository pattern on top of JPA.
- Quartz integration

Testing support is also provided, to allow you to do low level unit testing
of your CDI enabled projects. 

## Getting Started

The easiest way to get started with DeltaSpike is to use Maven or Gradle as a build tool, as described in [configuring in your project](http://deltaspike.apache.org/documentation/configure.html)

## Requirements to Build

- Git
- JDK 6
- Maven

Just run `mvn clean install` from the top level directory, `deltaspike` to build the source code.