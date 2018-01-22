# MQTT Bee

[![Build Status](https://travis-ci.com/mqtt-bee/mqtt-bee.svg?token=QxshYePt8tWLsG9B3pgf&branch=feature/BEE-6-mqtt-5-codecs)](https://travis-ci.com/mqtt-bee/mqtt-bee)

MQTT 5 compatible client library with a reactive API and back pressure support.


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

* As a starting point we will use the default code style of IntelliJ.
* The project uses its own @NotNull and @Nullable annotations, 
every non-primitive parameter should be annotated with one of them.
* Optional will only be used in the public API.

