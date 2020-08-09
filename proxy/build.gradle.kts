plugins {
    id("java-platform")
}


/* ******************** metadata ******************** */

description = "Adds dependencies for the HiveMQ MQTT Client proxy module"

metadata {
    moduleName = "com.hivemq.client.mqtt.proxy"
    readableName = "HiveMQ MQTT Client proxy module"
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
