plugins {
    `java-platform`
}


/* ******************** metadata ******************** */

description = "Adds dependencies for the HiveMQ MQTT Client epoll module"

metadata {
    moduleName.set("com.hivemq.mqtt.client2.epoll")
    readableName.set("HiveMQ MQTT Client epoll module")
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
