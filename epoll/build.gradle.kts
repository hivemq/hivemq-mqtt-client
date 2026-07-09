plugins {
    id("java-platform")
    id("io.github.sgtsilvio.gradle.maven-central-publishing")
}

/* ******************** metadata ******************** */

description = "Adds dependencies for the HiveMQ MQTT Client epoll module"

metadata {
    moduleName.set("com.hivemq.client.mqtt.epoll")
    readableName.set("HiveMQ MQTT Client epoll module")
}

/* ******************** dependencies ******************** */

javaPlatform {
    allowDependencies()
}

dependencies {
    api(rootProject)
    "runtime"(variantOf(libs.netty.transport.native.epoll) { classifier("linux-x86_64") })
}
