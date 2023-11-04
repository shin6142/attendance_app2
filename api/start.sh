#!/bin/sh

# stop existing daemons
./gradlew --stop

# check DO_MIGRATE environment variable and run liquibase migration if true
./gradlew update

# start
./gradlew bootRun