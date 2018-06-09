#!/usr/bin/env bash

echo "Standing up fileuploader application"
docker run -d \
    --network lcag-automation-network \
    -e "SPRING_PROFILES_ACTIVE=prod" \
	-e "SMTP_HOST=lcag-mail" \
	-e "SMTP_PORT=3025" \
	-e "SMTP_USERNAME=lcag-testing@lcag.com" \
	-e 'SMTP_PASSWORD=password' \
	-e "MYBB_FORUM_DATABASE_URL=jdbc:mysql://lcag-mysql/mybb" \
	-e "MYBB_FORUM_DATABASE_USERNAME=root" \
	-e "MYBB_FORUM_DATABASE_PASSWORD=p@ssword" \
	-e "EMAIL_SOURCE_URL=https://docs.google.com/document/d/1MKM84drgdaWRKWQo0HE-BV-NhZfzQH_cBF2wXvbsd4I/export?format=html" \
	-e "BCC_RECIPIENTS=test@bcc.com" \
	-e "EMAIL_FROM_NAME=LCAG" \
	-e "EMAIL_SUBJECT=LCAG Enquiry" \
	-e "VIRTUAL_PORT=8383" \
	-e "SERVER_PORT=8383" \
	--name fileuploader \
    -p 8383:8383
    -t dockernovinet/fileuploader

echo "Waiting for application status url to respond with 200"
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:8383/status)" != "200" ]]; do sleep 5; done