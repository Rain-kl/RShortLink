#!/bin/sh

set -eu

/opt/rshortlink/wait-for-dependencies.sh

exec java ${GATEWAY_JAVA_OPTS:-} -jar /opt/rshortlink/gateway.jar
