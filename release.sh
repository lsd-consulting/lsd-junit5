#!/bin/bash
set -e

# Publish to sonatype and then release to maven central
./gradlew publishToSonatype closeAndReleaseStagingRepository
