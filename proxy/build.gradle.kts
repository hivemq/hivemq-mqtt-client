plugins {
    id("java-platform")
    id("io.github.sgtsilvio.gradle.maven-central-publishing")
}


/* ******************** metadata ******************** */

description = "Adds dependencies for the HiveMQ MQTT Client proxy module"

metadata {
    moduleName.set("com.hivemq.client.mqtt.proxy")
    readableName.set("HiveMQ MQTT Client proxy module")
}


/* ******************** dependencies ******************** */

javaPlatform {
    allowDependencies()
}

dependencies {
    api(rootProject)
}

configurations.runtime {
    extendsFrom(rootProject.configurations["proxyImplementation"])
}
