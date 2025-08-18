plugins {
    `java-platform`
    alias(libs.plugins.mavenCentralPublishing)
}


/* ******************** metadata ******************** */

description = "Adds dependencies for the HiveMQ MQTT Client proxy module"

metadata {
    moduleName.set("com.hivemq.mqtt.client2.proxy")
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
