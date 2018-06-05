#!/usr/bin/env bash

echo "Standing up fileuploader application"
docker run -d \
    -e "SPRING_PROFILES_ACTIVE=prod" \
	-e "VIRTUAL_PORT=8383" \
	-e "SERVER_PORT=8383" \
	--name fileuploader \
    -p 8383:8383
    -t dockernovinet/fileuploader

echo "Waiting for application status url to respond with 200"
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:8383/status)" != "200" ]]; do sleep 5; done