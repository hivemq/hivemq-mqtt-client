# How to Release a New Version of the Java Client

1. Update the versions in [gradle.properties](https://github.com/hivemq/hivemq-mqtt-client/blob/master/gradle.properties#L1):
   - set `version` to the new release version
   - set `prevVersion` to the previous release version (the value `version` had before this bump).
     It drives the JAPICC binary/source compatibility check, which downloads that release and diffs the API against it.
2. Update the version in the dependency examples in [README.md](https://github.com/hivemq/hivemq-mqtt-client/blob/master/README.md) (Gradle and Maven snippets)
3. Create a new GitHub release with a v0.0.0 style tag

## Notes

1. Java client releases are done through a GitHub Action in [publish.yml](https://github.com/hivemq/hivemq-mqtt-client/blob/master/.github/workflows/publish.yml) that is triggered by publishing a new release.
2. The publish action publishes the Java client library to Maven using the version in `gradle.properties`.
3. Artifacts are usually downloadable from `repo1.maven.org` within ~10 minutes to a few hours; the `central.sonatype.com` search UI can lag up to ~4 hours. There is no guaranteed SLA.
4. Check here for an immediate view: https://repo1.maven.org/maven2/com/hivemq/hivemq-mqtt-client/
5. The Sonatype page for reference: https://central.sonatype.com/artifact/com.hivemq/hivemq-mqtt-client
