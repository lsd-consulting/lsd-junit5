# lsd-junit5
junit5 extension for generating lsd reports. (living sequence diagrams)



## Properties
The following properties are additional to the properties provided by lsd-core and can be overridden by adding a properties file called `lsd.properties` on the classpath of your
application.

| Property Name        | Default     | Description |
| ----------- | ----------- |------------ |
| lsd.junit5.hideStacktrace | false | Whether to display the stacktrace on the popup for aborted/failed tests. If the stacktrace needs to be disabled for any reason (e.g. approval tests where java line numbers don't match across builds then this can be enabled. |


