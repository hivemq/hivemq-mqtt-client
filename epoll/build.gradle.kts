plugins {
    id("java-platform")
}


/* ******************** metadata ******************** */

description = "Adds dependencies for the HiveMQ MQTT Client epoll module"

metadata {
    moduleName = "com.hivemq.client.mqtt.epoll"
    readableName = "HiveMQ MQTT Client epoll module"
}


/* ******************** dependencies ******************** */

javaPlatform {
    allowDependencies()
}

dependencies {
    api(rootProject)
}

configurations.runtime {
    extendsFrom(rootProject.configurations["epollImplementation"])
}
