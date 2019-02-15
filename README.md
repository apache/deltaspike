# Apache DeltaSpike

* [Documentation](https://deltaspike.apache.org)
* [Mailing Lists](http://deltaspike.apache.org/community.html#Mailinglists)
* [Contribution Guide](http://deltaspike.apache.org/source.html)
* [JIRA](https://issues.apache.org/jira/browse/DELTASPIKE)
* [Apache License v2.0](https://www.apache.org/licenses/LICENSE-2.0)


**Apache DeltaSpike** is a suite of portable CDI Extensions intended to make application development easier when working with CDI and Java EE.  

Contexts and Dependency Injection is a specification, published as: 
* JSR-299 (CDI-1.0) http://docs.jboss.org/cdi/spec/1.0/html/ 
* JSR-346 (CDI-1.2) http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html 
* JSR-365 (CDI-2.0) http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html 

Apache DeltaSpike is compatible with all those specification versions. 
Note that Apache DeltaSpike is **not** a CDI container itself, but a set of portable Extensions for it!


Some of key features of Apache DeltaSpike include:

- A core module that supports component configuration, type safe messaging and internationalization, and exception handling.
- A suite of utilities to make programmatic bean lookup easier.
- A plugin for Java SE to bootstrap both JBoss Weld, Apache OpenWebBeans and other CDI containers outside of a a JavaEE server.
- JSF integration, including backporting of JSF 2.2 features for Java EE 6.
- JPA integration and transaction support.
- A Data module, to create an easy to use repository pattern on top of JPA.
- Scheduler integration

Testing support is also provided, to allow you to do low level unit testing of your CDI enabled projects. 

## Getting Started

The easiest way to get started with DeltaSpike is to use Maven or Gradle as a build tool, as described in [configuring in your project](http://deltaspike.apache.org/documentation/configure.html)

## Requirements to Build

- Git
- JDK 8
- Maven

Just run `mvn clean install` from the top level directory, `deltaspike` to build the source code.