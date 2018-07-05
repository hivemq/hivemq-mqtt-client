# MQTT Bee

[![Build Status](https://travis-ci.org/mqtt-bee/mqtt-bee.svg?branch=develop)](https://travis-ci.org/mqtt-bee/mqtt-bee)

MQTT 5.0 and 3.1.1 compatible client library with a reactive API and back pressure support.

# Status
IMPORTANT: ALPHA STATUS, DO NOT USE IN PRODUCTION YET!

## DONE
 - Basic MQTT 3.1.1 and MQTT 5.0 functionality
 - Reactive API
 - Backpressure support for QoS 1 and 2
 - Additional MQTT 5 features: Server-Reauth

## TODO
 - APIs are not stable yet
 - Backpressure support for QoS 0
 - Reconnect Handling and Message redelivery
 - SSL integration (branch need to be integrated)
 - Websocket support
 - Simpler APIs
 - Disk Persistence

# Example
tbd

## How to build

JDK 8 is required.

## Branching model

* master: the release branch
* develop: where features are merged into
* every feature or bugfix will have its own branch, branched from develop, 
it will be merged after the code review of the pull request

### Branching guidelines

* branch types: feature, bugfix
* branch names: starting with feature/, bugfix/ + lower case name of the task, spaces replaced with -
* also branches from a feature branch are called feature/ or bugfix/ 
whether it contributes to the feature as a sub-feature or a bugfix

### Commit message guidelines

* Commits in feature/bugfix branches should be as atomic as possible.
* Commits in feature/bugfix branches do not have to follow strict guidelines, 
but they should describe the changes clearly.
* When feature/bugfix branches are merged into develop, the commit message should follow these guidelines: tbd

## Code style guidelines

* The project follows the [AOSP Java Code Style](https://source.android.com/setup/contribute/code-style).
  * The Gradle build is configured to use the [Spotless](https://github.com/diffplug/spotless/tree/master/plugin-gradle)
    Gradle plugin to check for conformity, and fail if the code is not formatted correctly.
  * Use `./gradlew spotlessApply` to format your source code using the plugin.
* The project uses its own @NotNull and @Nullable annotations, 
every non-primitive parameter should be annotated with one of them.
* Optional will only be used in the public API.
* The project uses [checkstyle](http://checkstyle.sourceforge.net/index.html) to enforce the
  [AOSP conding standard](https://source.android.com/setup/contribute/code-style).
  * The Gradle build is configured to use the [Checkstyle Plugin](https://docs.gradle.org/current/userguide/checkstyle_plugin.html)
    to check for conformity. The [checkstyle config](config/checkstyle/checkstyle.xml) is located in
    `config/checkstyle/checkstyle.xml` and is derived from the [Google Checks config](https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/google_checks.xml)
    provided by the checkstyle project. 
  * Use `./gradlew checkstyleMain checkstyleTest` to generate a checkstyle report and fix reported
    errors and warnings.

### Developing with IntelliJ IDEA

* If using IntelliJ for development, we strongly recommend installing the [google-java-format](https://plugins.jetbrains.com/plugin/8527-google-java-format)
  plugin. Make sure to install a version of the plugin that uses version 1.6 of the
  google-java-format tool.
  * See also the [usage instructions of google-java-format in IntelliJ](https://github.com/google/google-java-format#intellij).
* To use the correct import ordering, use the [IntelliJ Java AOSP Style file](config/intellij/intellij-java-aosp-style.xml)
  provided by the project in the `config/intellij/` directory. Import the style file into
  `IntelliJ Preferences > Editor > Code Style`.
* Use the [CheckStyle-IDEA plugin](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea) to
  ensure compliance to our coding standard.
  * Make sure to install a version of the plugin that is compatible with the checkstyle version in
    `build.gradle`.
  * Import the checkstyle setting into `IntelliJ Preferences > Other Settings > Checkstyle`.
  