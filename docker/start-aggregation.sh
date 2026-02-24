#!/bin/sh

set -eu

/opt/rshortlink/wait-for-dependencies.sh

exec java ${AGGREGATION_JAVA_OPTS:-} -jar /opt/rshortlink/aggregation.jar
