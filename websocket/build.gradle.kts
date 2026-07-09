plugins {
    id("java-platform")
    id("io.github.sgtsilvio.gradle.maven-central-publishing")
}

/* ******************** metadata ******************** */

description = "Adds dependencies for the HiveMQ MQTT Client websocket module"

metadata {
    moduleName.set("com.hivemq.client.mqtt.websocket")
    readableName.set("HiveMQ MQTT Client websocket module")
}

/* ******************** dependencies ******************** */

javaPlatform {
    allowDependencies()
}

dependencies {
    api(rootProject)
    "runtime"(libs.netty.codec.http)
}
