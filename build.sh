#!/bin/bash
set -e
set -x

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# Disable the downloading/downloaded message: https://stackoverflow.com/questions/21638697/disable-maven-download-progress-indication
export MAVEN_OPTS="$MAVEN_OPTS -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
if [ "$JDK" != "" ]; then
  export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m $MAVEN_OPTS"
else
  export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m $MAVEN_OPTS"
fi

if [ "$JDK" = "9" ]; then
  export JAVA_HOME="/usr/lib/jvm/java-9-oracle/"
fi

mvn -version

PROPERTIES="$PROPERTIES -Duser.country=US -Duser.language=en"

if [ "$BUILD_JDK" != "" ]; then
  PROPERTIES="$PROPERTIES -Djava.version=$BUILD_JDK"
fi

exec mvn -B -P "${PROFILES}" clean install -V $PROPERTIES