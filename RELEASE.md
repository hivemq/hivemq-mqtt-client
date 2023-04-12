# How to Release a New Version of the Java Client

1. Bump the version number in [gradle.properties](https://github.com/hivemq/hivemq-mqtt-client/blob/master/gradle.properties#L1)
2. Create a new Github release with a v0.0.0 style tag

## Notes

1. Java client releases are done through a Github Action in [publish.yml](https://github.com/hivemq/hivemq-mqtt-client/blob/master/.github/workflows/publish.yml) that is triggered by publishing a new release.
2. The publish action publishes the Java client library to Maven using the version in `gradle.properties`.
3. The Maven/Sonatype UIs take up to 24 hours to show the new version (facepalm).
4. Check here for an immediate view: https://repo1.maven.org/maven2/com/hivemq/hivemq-mqtt-client/
5. The Sonatype page for reference: https://central.sonatype.com/artifact/com.hivemq/hivemq-mqtt-client

