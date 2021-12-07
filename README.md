[![semantic-release](https://img.shields.io/badge/semantic-release-e10079.svg?logo=semantic-release)](https://github.com/semantic-release/semantic-release)

# lsd-junit5

[![Build](https://github.com/lsd-consulting/lsd-junit5/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/lsd-consulting/lsd-junit5/actions/workflows/ci.yml)
[![Nightly Build](https://github.com/lsd-consulting/lsd-junit5/actions/workflows/nightly.yml/badge.svg)](https://github.com/lsd-consulting/lsd-junit5/actions/workflows/nightly.yml)
[![GitHub release](https://img.shields.io/github/release/lsd-consulting/lsd-junit5)](https://github.com/lsd-consulting/lsd-junit5/releases)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lsd-consulting/lsd-junit5.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.lsd-consulting%22%20AND%20a:%22lsd-junit5%22)

Provides a Junit5 Extension for generating test reports with sequence diagrams. (Using the [lsd-core](https://github.com/lsd-consulting/lsd-core) library)

## Properties
The following properties are additional to the properties provided by lsd-core and can be overridden by adding a properties file called `lsd.properties` on the classpath of your
application.

| Property Name        | Default     | Description |
| ----------- | ----------- |------------ |
| lsd.junit5.hideStacktrace | false | Whether to display the stacktrace on the popup for aborted/failed tests. If the stacktrace needs to be disabled for any reason (e.g. approval tests where java line numbers don't match across builds then this can be enabled. |


