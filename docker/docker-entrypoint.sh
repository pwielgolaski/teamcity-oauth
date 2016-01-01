#!/bin/bash
set -e

j2 /auth-config.xml.j2 > ${TEAMCITY_DATA_PATH}/config/auth-config.xml
echo "Starting teamcity..."
exec /opt/TeamCity/bin/teamcity-server.sh run