/*
Credentials can be stored in a local file named credentials.gradle, which will be ignored by git, in the following
format:

ext {
    bintray_username = '...'
    bintray_apiKey = '...'
    github_token = '...'
}
*/
if (file("${project.rootDir}/gradle/credentials.gradle").exists()) {
    apply("${project.rootDir}/gradle/credentials.gradle")
}

/*
Alternatively they can be specified via environment variables:
e.g. via shell script, then either source that script in your shell or call gradle from the script:

#!/bin/sh
export bintray_username="..."
export bintray_apiKey="..."
export github_token="..."

Secure configuration for Travis CI:
Credentials must be stored in the Travis repository settings (https://travis-ci.org/hivemq/hivemq-mqtt-client/settings).
The environment variables are encrypted by Travis and get decrypted before each build.
Availability must be restricted to the master branch (only needed for publishing releases).
*/
listOf("bintray_username", "bintray_apiKey", "github_token").forEach {
    if (!project.hasProperty(it)) {
        project.extra[it] = System.getenv()[it]
    }
}
