#!/usr/bin/env bash
./gradlew clean && ./gradlew publish
# allow a user to run "bash publish.sh skip" to skip deploying to mvn central.
if [ -z "$1" ]; then
    ./gradlew jreleaserDeploy --stacktrace
else
    ./gradlew jreleaserDeploy --dryrun --stacktrace
fi
