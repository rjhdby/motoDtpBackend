#!/usr/bin/env bash
./gradlew clean build

if [[ $? -ne 0 ]]
then
  echo "Build error"
  exit 1
fi

docker-compose -f docker-compose.yml stop
if [[ "$1" == "--stop" ]]
then
  exit 0
fi

docker-compose -f docker-compose.yml build
docker-compose -f docker-compose.yml up