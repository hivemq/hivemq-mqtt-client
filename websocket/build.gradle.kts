plugins {
    id("java-platform")
}


/* ******************** metadata ******************** */

description = "Adds dependencies for the HiveMQ MQTT Client websocket module"

metadata {
    moduleName = "com.hivemq.client.mqtt.websocket"
    readableName = "HiveMQ MQTT Client websocket module"
}


/* ******************** dependencies ******************** */

javaPlatform {
    allowDependencies()
}

dependencies {
    api(rootProject)
}

configurations.runtime {
    extendsFrom(rootProject.configurations["websocketImplementation"])
}
