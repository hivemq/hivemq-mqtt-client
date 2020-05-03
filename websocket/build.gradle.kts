plugins {
    id("java-platform")
}


/* ******************** metadata ******************** */

description = "Adds dependencies for the HiveMQ MQTT Client websocket module"

extra["moduleName"] = "com.hivemq.client.mqtt.websocket"
extra["readableName"] = "HiveMQ MQTT Client websocket module"


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
