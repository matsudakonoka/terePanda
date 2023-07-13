#!/bin/sh

# Fail on a single failed command in a pipeline (if supported)
(set -o | grep -q pipefail) && set -o pipefail

# Fail on error and undefined vars
set -e

# PINPOINT_AGENT
PINPOINT_AGENT="/deployments/pinpoint-agent/pinpoint-bootstrap-1.7.3.jar"

# Workdir
WORKDIR=/deployments
# Init pinpoint
/deployments/configure-agent.sh

# Java
echo "The application will start in ${APP_SLEEP}s..." && sleep ${APP_SLEEP}

# Get agent_id
AGENT_ID_PREFIX=${APP_NAME_ID}
AGENT_ID_SUFFIX="$(echo $HOSTNAME | rev | cut -d'-' -f-1 | rev)"
AGENT_ID="$AGENT_ID_PREFIX-$AGENT_ID_SUFFIX"

if [ -z "${APP_NAME_ID:-}" ]; then
    exec java ${JAVA_OPTS} \
              -server -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=${DEFAULT_MAXRAM:-2} \
              -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${JDWP_PORT:-8301} \
              -Djava.security.egd=file:/dev/./urandom \
              -jar "${WORKDIR}/${JAVA_APP_JAR_NAME:-app.jar}" ${RUN_ARGS} "$@"
else
    # Filebeat
    nohup /deployments/filebeat-6.8.0/filebeat -e -c /deployments/filebeat-6.8.0/filebeat.yml 1>/deployments/logs/filebeat.log 2>&1 &
    # Java
    exec java ${JAVA_OPTS} \
              -server -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=${DEFAULT_MAXRAM:-2} \
              -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${JDWP_PORT:-8301} \
              -Djava.security.egd=file:/dev/./urandom \
              -javaagent:${JAVA_AGENT:-${PINPOINT_AGENT}} \
              -Dpinpoint.agentId=${AGENT_ID} \
              -Dpinpoint.applicationName=${APP_NAME_ID} \
              -jar "${WORKDIR}/${JAVA_APP_JAR_NAME:-app.jar}" ${RUN_ARGS} "$@"
fi