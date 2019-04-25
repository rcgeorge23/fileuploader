#!/usr/bin/env bash

function rmContainer {
    CONTAINER_NAME=$1
    docker rm -f $(docker ps -a | grep $CONTAINER_NAME | cut -c 1-12)
}

echo "Removing all containers"
rmContainer lcag-membership-form ; \
    rmContainer lcag-sftp ; \
    rmContainer lcag-mysql ; \
    rmContainer lcag-mail

exit 0