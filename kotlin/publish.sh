#!/usr/bin/env bash
./gradlew clean && ./gradlew publish && ./gradlew jreleaserDeploy --stacktrace
