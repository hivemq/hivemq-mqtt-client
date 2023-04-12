# How to Release a New Version of the Java Client

1. Bump the version number in [gradle.properties](https://github.com/hivemq/hivemq-mqtt-client/blob/master/gradle.properties#L1)
2. Create a new Github release with a v0.0.0 style tag

## Notes

Java client releases are done through a Github Action in [publish.yml](https://github.com/hivemq/hivemq-mqtt-client/blob/master/.github/workflows/publish.yml) that is triggered by publishing a new release.

The publish action publishes the Java client library to Maven using the version in `gradle.properties`.

