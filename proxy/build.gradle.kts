plugins {
    id("java-platform")
}


/* ******************** metadata ******************** */

description = "Adds dependencies for the HiveMQ MQTT Client proxy module"

extra["moduleName"] = "com.hivemq.client.mqtt.proxy"
extra["readableName"] = "HiveMQ MQTT Client proxy module"


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
