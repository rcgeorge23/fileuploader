#!/usr/bin/env bash

APPLICATION_HOST=${1:-"localhost"}
APPLICATION_PORT=${2:-"8282"}

./standup_supporting_containers.sh

./standup_application.sh ${APPLICATION_HOST} ${APPLICATION_PORT}

echo "Waiting for application status url to respond with 200. Status url: http://${APPLICATION_HOST}:${DASHBOARD_PORT}/status"
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' ${APPLICATION_HOST}:${APPLICATION_PORT}/status)" != "200" ]]; do sleep 5; done